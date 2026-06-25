#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
通用：从符合版式的 docx 批量生成 user_detection_type / user_detection_question SQL。

版式与 ruankao_doc_parse.extract_questions 一致：(n)A.…、【答案】、【解析】。

用法（建议用参数；也可用环境变量，见 SKILL.md）：

  python3 generate_generic_docx_sql.py \\
    --type-prefix RK_PROG \\
    --exam-name 程序员 \\
    --out-dir …/sql/arelore_education/软考/初级/程序员 \\
    --root …/程序员历年真题 \\
    --recursive \\
    --name-substr 答案
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

from ruankao_doc_parse import (
    append_unofficial_suffix,
    docx_story_text_with_images,
    emit_sql,
    extract_questions,
    infer_unofficial_exam,
    question_has_options,
)


def _resources_root() -> Path:
    return Path(__file__).resolve().parent.parent.parent


def infer_term_from_text(text: str) -> Optional[str]:
    """从正文开头推断上/下半年（文件名无「上/下半年」时）。"""
    head = (text or "")[:800]
    m = re.search(r"(20[12]\d|19\d{2})年([上下])半年", head)
    if m:
        return m.group(2)
    if "上半年" in head and "下半年" not in head:
        return "上"
    if "下半年" in head:
        return "下"
    return None


def parse_soft_exam_filename(filename: str, *, text_hint: str = "") -> Optional[Dict[str, str]]:
    """从文件名解析年份、上/下半年、可选批次（软考常见命名）。"""
    name = filename
    m_full = re.search(r"((?:19|20)\d{2})年全年", name)
    if m_full:
        year = m_full.group(1)
        half_cn = infer_term_from_text(text_hint) or "上"
        term = "H1" if half_cn == "上" else "H2"
        return {
            "year": year,
            "half_cn": half_cn,
            "term": half_cn + "半年",
            "term_code": term,
            "batch": "",
        }

    m = re.search(r"((?:19|20)\d{2})(?:年)?([上下])半年", name)
    if not m:
        m0 = re.search(r"((?:19|20)\d{2})年([上下])(?!半年)", name)
        if m0:
            year, half_cn = m0.group(1), m0.group(2)
            term = "H1" if half_cn == "上" else "H2"
            batch_m = re.search(r"[（(]第\s*(\d+)\s*批[）)]", name)
            batch = batch_m.group(1) if batch_m else ""
            return {
                "year": year,
                "half_cn": half_cn,
                "term": half_cn + "半年",
                "term_code": term,
                "batch": batch,
            }
        m2 = re.search(r"([上下])半年\s*((?:19|20)\d{2})\s*年", name)
        if m2:
            year, half_cn = m2.group(2), m2.group(1)
        else:
            m3 = re.search(r"((?:19|20)\d{2})", name)
            if not m3:
                return None
            year = m3.group(1)
            if "上" in name and "下" not in name:
                half_cn = "上"
            elif "下" in name:
                half_cn = "下"
            else:
                half_cn = infer_term_from_text(text_hint) or "下"
    else:
        year, half_cn = m.group(1), m.group(2)

    term = "H1" if half_cn == "上" else "H2"
    batch_m = re.search(r"[（(]第\s*(\d+)\s*批[）)]", name)
    batch = batch_m.group(1) if batch_m else ""
    return {"year": year, "half_cn": half_cn, "term": half_cn + "半年", "term_code": term, "batch": batch}


def infer_session_suffix(filename: str, default: str) -> str:
    n = filename
    if "综合知识" in n:
        return "JC"
    if "试题与答案" in n or "真题试题" in n:
        return "JC"
    if "下半年" in n or "下午" in n or "案例分析" in n:
        return "YY"
    if "上半年" in n or "上午" in n or "基础知识" in n:
        return "JC"
    if "应用技术" in n:
        return "YY"
    if "答案详解" in n or "答案解析" in n or "真题+答案" in n or "真题＋答案" in n:
        return "JJ"
    if "答案" in n and ("试题" in n or "真题" in n):
        return "JJ"
    return default


def build_type_code(prefix: str, year: str, term: str, batch: str, session_suffix: str) -> str:
    if batch:
        return f"{prefix}_{year}{term}_B{batch}_{session_suffix}"
    return f"{prefix}_{year}{term}_{session_suffix}"


def convert_doc_to_docx(doc_path: Path, cache_dir: Path) -> Optional[Path]:
    """macOS 使用 textutil 将 .doc 转为临时 docx。"""
    if doc_path.suffix.lower() != ".doc" or doc_path.name.startswith(".~"):
        return None
    out = cache_dir / f"{doc_path.stem}.docx"
    if out.is_file() and out.stat().st_mtime >= doc_path.stat().st_mtime:
        return out
    try:
        subprocess.run(
            ["textutil", "-convert", "docx", "-output", str(out), str(doc_path)],
            check=True,
            capture_output=True,
        )
    except (subprocess.CalledProcessError, OSError) as e:
        print(f"SKIP doc 转换失败 {doc_path.name}: {e}", file=sys.stderr)
        return None
    return out if out.is_file() else None


def resolve_docx_path(path: Path, cache_dir: Path) -> Optional[Path]:
    if path.suffix.lower() == ".docx":
        return path
    if path.suffix.lower() == ".doc":
        return convert_doc_to_docx(path, cache_dir)
    return None


def collect_word_files(root: Path, recursive: bool, name_substr: Optional[str]) -> List[Path]:
    if not root.is_dir():
        return []
    patterns = ("*.docx", "*.doc") if recursive else ("*.docx", "*.doc")
    out: List[Path] = []
    for pat in patterns:
        it = root.rglob(pat) if recursive else root.glob(pat)
        for p in sorted(it):
            if not p.is_file():
                continue
            if p.name.startswith(".~") or p.name.startswith("."):
                continue
            if name_substr and name_substr not in p.name:
                continue
            out.append(p)
    # 同名 doc/docx 只保留 docx
    by_stem: Dict[str, Path] = {}
    for p in out:
        stem = p.stem
        if stem not in by_stem or p.suffix.lower() == ".docx":
            by_stem[stem] = p
    return sorted(by_stem.values())


def validate_docx(path: Path) -> bool:
    try:
        with zipfile.ZipFile(path) as z:
            return "word/document.xml" in z.namelist()
    except (zipfile.BadZipFile, OSError):
        return False


def main() -> None:
    ap = argparse.ArgumentParser(description="通用 docx → 软考检测题库 SQL")
    ap.add_argument("--type-prefix", default=os.environ.get("RUANKAO_TYPE_PREFIX", ""), help="如 RK_PROG、RK_IPMP")
    ap.add_argument("--exam-name", default=os.environ.get("RUANKAO_EXAM_NAME", ""), help="考试名称，写入 extra_info.exam")
    ap.add_argument(
        "--exam-level",
        default=os.environ.get("RUANKAO_EXAM_LEVEL", ""),
        help="可选，如 初级、高级，写入 type_description",
    )
    ap.add_argument("--out-dir", type=Path, default=None, help="产出 .sql 的目录")
    ap.add_argument("--root", type=Path, default=None, help="扫描 docx 的根目录（与 positional 二选一）")
    ap.add_argument("--recursive", action="store_true", help="递归子目录")
    ap.add_argument("--name-substr", default=os.environ.get("RUANKAO_NAME_SUBSTR", ""), help="仅处理文件名包含该子串的 docx")
    ap.add_argument(
        "--default-session-suffix",
        default=os.environ.get("RUANKAO_DEFAULT_SESSION_SUFFIX", "GK"),
        help="无法从文件名推断试卷类型时的 type_code 后缀（默认 GK，如信管综合）",
    )
    ap.add_argument(
        "docx",
        nargs="*",
        type=Path,
        help="直接指定若干 docx/doc（指定后忽略 --root）",
    )
    args = ap.parse_args()

    type_prefix = (args.type_prefix or "").strip()
    exam_name = (args.exam_name or "").strip()
    if not type_prefix or not exam_name:
        ap.error("必须提供 --type-prefix 与 --exam-name（或对应环境变量）")

    out_dir = args.out_dir
    if out_dir is None:
        env_out = os.environ.get("RUANKAO_OUT_DIR")
        if not env_out:
            ap.error("必须提供 --out-dir 或环境变量 RUANKAO_OUT_DIR")
        out_dir = Path(env_out).expanduser()
    out_dir = out_dir.resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    if args.docx:
        files = [p.resolve() for p in args.docx]
    else:
        root = args.root
        if root is None:
            env_root = os.environ.get("RUANKAO_DOCX_ROOT")
            if not env_root:
                ap.error("未传入 docx 文件时，必须提供 --root 或 RUANKAO_DOCX_ROOT")
            root = Path(env_root).expanduser()
        root = root.resolve()
        rec = args.recursive or os.environ.get("RUANKAO_RECURSIVE", "").lower() in ("1", "true", "yes")
        substr = args.name_substr.strip() or None
        files = collect_word_files(root, rec, substr)

    if not files:
        print("未找到可处理的 doc/docx", file=sys.stderr)
        sys.exit(1)

    max_n = os.environ.get("RUANKAO_MAX_DOCX")
    if max_n and max_n.isdigit():
        files = files[: int(max_n)]

    cache_dir = Path(tempfile.gettempdir()) / "ruankao_docx_cache"
    cache_dir.mkdir(parents=True, exist_ok=True)

    for src in files:
        print(f"→ {src}", flush=True)
        doc = resolve_docx_path(src, cache_dir)
        if doc is None:
            print(f"SKIP 无法解析: {src.name}", file=sys.stderr)
            continue
        if not validate_docx(doc):
            print(f"SKIP 非有效 docx: {doc.name}", file=sys.stderr)
            continue
        try:
            text, image_blobs = docx_story_text_with_images(doc)
        except (zipfile.BadZipFile, OSError, KeyError) as e:
            print(f"SKIP 读取失败 {src.name}: {e}", file=sys.stderr)
            continue

        meta = parse_soft_exam_filename(src.name, text_hint=text)
        if not meta:
            print(f"SKIP 无法解析年份/场次: {src.name}", file=sys.stderr)
            continue

        session_suffix = infer_session_suffix(src.name, args.default_session_suffix)
        type_code = build_type_code(
            type_prefix, meta["year"], meta["term_code"], meta["batch"], session_suffix
        )

        session_cn = {
            "JJ": "答案详解",
            "JC": "综合知识",
            "YY": "案例分析",
            "GK": "客观题",
        }.get(session_suffix, session_suffix)

        batch_label = f"第{meta['batch']}批" if meta["batch"] else ""
        type_name = (
            f"软考-{exam_name}-{meta['year']}年{meta['half_cn']}半年"
            + (f"-{batch_label}" if batch_label else "")
            + f"-{session_cn}"
        )
        type_desc = (
            f"全国计算机技术与软件专业技术资格（水平）考试 {exam_name}"
            + (f"（{args.exam_level}）" if args.exam_level else "")
            + f" {meta['year']}年{meta['half_cn']}半年 {session_cn}"
            + (f"（{batch_label}）" if batch_label else "")
            + f"（来源：{src.name}）"
        )

        qs = extract_questions(text)
        if not qs:
            print(
                f"WARN 0 题: {src.name} — 需符合 SKILL 中任一版式（含无选项案例分析）",
                file=sys.stderr,
            )
            continue

        objective_count = sum(1 for q in qs if question_has_options(q))
        unofficial = infer_unofficial_exam(
            src.name,
            text,
            len(qs),
            session_suffix,
            objective_count=objective_count,
        )
        if unofficial:
            type_name = append_unofficial_suffix(type_name)

        safe_stem = re.sub(r"[^\w\u4e00-\u9fff]+", "_", src.stem).strip("_")[:80]
        out_name = f"软考_{exam_name}_{meta['year']}{meta['half_cn']}半年_{safe_stem}_导入.sql"
        out_path = out_dir / out_name

        emit_meta: Dict[str, object] = {
            "source": src.name,
            "exam": exam_name,
            "year": meta["year"],
            "term": meta["term"],
            "session": session_cn,
        }
        if args.exam_level:
            emit_meta["level"] = args.exam_level
        if meta.get("batch"):
            emit_meta["batch"] = meta["batch"]
        if unofficial:
            emit_meta["unofficial"] = True

        emit_sql(
            type_code=type_code,
            type_name=type_name,
            type_desc=type_desc,
            meta=emit_meta,
            questions=qs,
            image_blobs=image_blobs,
            source_doc=src.name,
            out_path=out_path,
        )
        print(out_path, len(qs), src.name)


if __name__ == "__main__":
    main()
