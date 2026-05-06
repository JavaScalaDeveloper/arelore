#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从「系统架构设计师 答案详解」docx 抽取客观题，生成：
- arelore_common.common_binary_file（正文内 DrawingML 图片，按 SHA-256 去重）
- arelore_education.user_detection_type / user_detection_question

正文文本由 document.xml 按段落解析；图片输出为占位符 【img:sha256】，与题目 extra_info.imageHashValues 一致。
"""
from __future__ import annotations

import hashlib
import html
import json
import re
import sys
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
A_NS = "http://schemas.openxmlformats.org/drawingml/2006/main"

IMG_PLACEHOLDER = re.compile(r"【img:([0-9a-f]{64})】")


def strip_question_noise(s: str) -> str:
    """
    去掉题目/解析/选项中夹带的出版说明类括注（如「（答案详解）」），不影响【答案】【解析】标记。
    """
    if not s:
        return s
    t = s
    patterns = [
        r"[（(]\s*答案详解\s*[）)]",
        r"[（(]\s*答案\s*与\s*详解\s*[）)]",
        r"[（(]\s*试题详解\s*[）)]",
        r"[（(]\s*解析与答案\s*[）)]",
        r"【\s*答案详解\s*】",
        r"【\s*试题详解\s*】",
    ]
    for _ in range(3):
        prev = t
        for p in patterns:
            t = re.sub(p, "", t, flags=re.I)
        if t == prev:
            break
    t = re.sub(r"[ \t]{2,}", " ", t)
    t = re.sub(r"\n{3,}", "\n\n", t)
    return t.strip()


def _load_document_rels(z: zipfile.ZipFile) -> Dict[str, str]:
    root = ET.fromstring(z.read("word/_rels/document.xml.rels"))
    rels: Dict[str, str] = {}
    for rel in root:
        if rel.tag.split("}")[-1] != "Relationship":
            continue
        rid = rel.attrib.get("Id")
        tgt = rel.attrib.get("Target")
        if rid and tgt:
            rels[rid] = tgt.replace("\\", "/")
    return rels


def _sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest().lower()


def _paragraph_chunks(
    paragraph: ET.Element,
    rels: Dict[str, str],
    z: zipfile.ZipFile,
    blob_by_hex: Dict[str, bytes],
) -> List[Tuple[str, str]]:
    """按阅读顺序返回 ('t', 文本) 或 ('i', sha256hex)。"""
    parts: List[Tuple[str, str]] = []
    for run in paragraph.findall(f".//{{{W_NS}}}r"):
        for node in run.iter():
            if node == run:
                continue
            if node.tag == f"{{{W_NS}}}t" and node.text:
                parts.append(("t", node.text))
            elif node.tag == f"{{{A_NS}}}blip":
                rid = node.attrib.get(f"{{{R_NS}}}embed") or node.attrib.get("embed")
                if not rid:
                    continue
                tgt = rels.get(rid)
                if not tgt:
                    continue
                zip_path = "word/" + tgt.lstrip("/")
                try:
                    data = z.read(zip_path)
                except KeyError:
                    continue
                hx = _sha256_hex(data)
                blob_by_hex.setdefault(hx, data)
                parts.append(("i", hx))
    return parts


def docx_story_text_with_images(path: Path) -> Tuple[str, Dict[str, bytes]]:
    """
    从 document.xml 正文按段落输出纯文本；DrawingML 图片占位为 【img:sha256小写】。
    返回 (全文, sha256hex -> 二进制) 供写入 common_binary_file。
    """
    blob_by_hex: Dict[str, bytes] = {}
    lines: List[str] = []
    with zipfile.ZipFile(path) as z:
        rels = _load_document_rels(z)
        root = ET.fromstring(z.read("word/document.xml"))
        body = root.find(f"{{{W_NS}}}body")
        if body is None:
            return "", {}
        for p in body.iter(f"{{{W_NS}}}p"):
            chunks = _paragraph_chunks(p, rels, z, blob_by_hex)
            line = "".join((tx if k == "t" else f" 【img:{tx}】 ") for k, tx in chunks)
            lines.append(line)
    text = "\n".join(lines)
    text = html.unescape(text)
    text = text.replace("\u3000", " ").replace("\xa0", " ")
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip() + "\n", blob_by_hex


def ordered_image_hashes(*text_parts: str) -> List[str]:
    seen = set()
    out: List[str] = []
    for s in text_parts:
        if not s:
            continue
        for m in IMG_PLACEHOLDER.finditer(s):
            h = m.group(1).lower()
            if h not in seen:
                seen.add(h)
                out.append(h)
    return out


def truncate_avoid_img_token(s: str, n: int) -> str:
    """varchar(255) 截断时避免在 【img:64位hex】 中间切断。"""
    s = re.sub(r"[ \t]+", " ", s).strip()
    if len(s) <= n:
        return s
    limit = n - 1
    cut = limit
    for m in IMG_PLACEHOLDER.finditer(s):
        if m.start() < n <= m.end():
            cut = min(cut, m.start())
    if cut < 1:
        return ""
    if cut < limit:
        return s[:cut].rstrip() + "…"
    return s[:limit] + "…"


def clean_answer_line(s: str) -> str:
    s = s.replace("【答案】", "").strip()
    s = re.sub(r"\s+", " ", s)
    return s.strip()


def split_multi_answers(ans: str) -> List[str]:
    parts = re.split(r"[\s、,，]+", ans)
    return [p.strip().upper() for p in parts if p.strip() and len(p.strip()) == 1]


def parse_options_one_line(line: str) -> Optional[Tuple[int, Dict[str, str]]]:
    """
    解析形如 (12)A.xxxB.xxxC.xxxD.xxx 的单行选项（A-G）。
    注意：正则第二组从 A 选项正文开始，需拼回 \"A.\" 再切分各选项。
    """
    m = re.match(r"^\((\d+)\)\s*A[\.．、]\s*(.+)$", line.strip(), re.I)
    if not m:
        return None
    num = int(m.group(1))
    rest = m.group(2).strip()
    one = re.sub(r"\s+", " ", "A." + rest)
    # 仅识别「字母 + 点/顿号」形式的选项键，避免英文单词中的字母被误识别
    found: Dict[str, str] = {}
    marks = list(re.finditer(r"([A-G])[\.．、]\s*", one, re.I))
    if len(marks) < 2:
        return None
    for i, km in enumerate(marks):
        k = km.group(1).upper()
        start = km.end()
        end = marks[i + 1].start() if i + 1 < len(marks) else len(one)
        found[k] = strip_question_noise(one[start:end].strip())
    if len(found) < 2:
        return None
    return num, found


def extract_questions(text: str) -> List[dict]:
    """
    按文档顺序扫描：cursor 到下一处【答案】之间为「题干+选项」，
    支持同一【答案】行内多个字母对应连续多道 (n)A… 小题。
    """
    text = text.replace("\r", "")
    out: List[dict] = []
    cursor = 0
    while True:
        p = text.find("【答案】", cursor)
        if p == -1:
            break
        prev = text[cursor:p]
        matches = list(re.finditer(r"\((\d+)\)\s*A[\.．、]\s*", prev))
        if not matches:
            cursor = p + 4
            continue

        ans_line_end = text.find("\n", p)
        if ans_line_end == -1:
            ans_line_end = len(text)
        ans_raw = text[p:ans_line_end]
        letters = split_multi_answers(clean_answer_line(ans_raw))
        if not letters:
            cursor = p + 4
            continue

        pd = text.find("【解析】", ans_line_end)
        if pd == -1:
            cursor = p + 4
            continue
        exp_start = pd + len("【解析】")
        nex = re.search(r"\n\(\d+\)\s*A[\.．、]\s*", text[exp_start:])
        if nex:
            raw_block = text[exp_start : exp_start + nex.start()]
            # 解析后常紧跟下一题题干（含（n））；从解析正文中剔除该题干尾巴，cursor 放到题干开头供下一题使用
            trim = re.search(r"(\n\n[^\n]+（\d+）[^\n]*。)\s*$", raw_block)
            if trim:
                raw_exp = raw_block[: trim.start(1)]
                exp = raw_exp.strip()
                cursor = exp_start + trim.start(1)
            else:
                raw_exp = raw_block
                exp = raw_exp.strip()
                cursor = exp_start + len(raw_block)
        else:
            raw_exp = text[exp_start:]
            exp = raw_exp.strip()
            cursor = exp_start + len(raw_exp)

        if len(letters) <= len(matches):
            chosen = matches[-len(letters) :]
        else:
            chosen = [matches[-1]]
            letters = letters[:1]

        shared_stem = strip_question_noise(prev[: chosen[0].start()].strip())
        for i, (mi, letter) in enumerate(zip(chosen, letters)):
            opt_start = mi.start()
            opt_end = chosen[i + 1].start() if i + 1 < len(chosen) else p
            opt_line = prev[opt_start:opt_end].strip()
            parsed = parse_options_one_line(opt_line.replace("\n", " "))
            if not parsed:
                continue
            num, opts_map = parsed
            out.append(
                {
                    "order": num,
                    "stem": shared_stem,
                    "options": opts_map,
                    "answer_keys": [letter],
                    "explanation": strip_question_noise(exp),
                }
            )

        while cursor < len(text) and text[cursor] == "\n":
            cursor += 1

    seen = set()
    uniq: List[dict] = []
    for q in sorted(out, key=lambda x: x["order"]):
        if q["order"] in seen:
            continue
        seen.add(q["order"])
        uniq.append(q)
    return uniq


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def build_options_json(q: dict) -> str:
    letter = q["answer_keys"][0].upper()
    opts = []
    for key in sorted(q["options"].keys()):
        is_correct = key.upper() == letter
        opts.append(
            {
                "key": key,
                "text": strip_question_noise(q["options"][key]),
                "scores": {"score": 1 if is_correct else 0},
            }
        )
    return json.dumps(opts, ensure_ascii=False, separators=(",", ":"))


def build_extra_info(q: dict, full_stem: str, full_exp: str) -> str:
    opt_blob = " ".join(
        strip_question_noise(q["options"][k]) for k in sorted(q["options"].keys())
    )
    d = {
        "questionType": "单选题",
        "sourceQuestionNo": q["order"],
        "correctOptionKey": q["answer_keys"][0],
        "explanation": full_exp[:8000],
        "imageHashValues": ordered_image_hashes(full_stem, full_exp, opt_blob),
    }
    if len(full_stem) > 255 or IMG_PLACEHOLDER.search(full_stem):
        d["fullQuestionName"] = full_stem
    if len(full_exp) > 255 or IMG_PLACEHOLDER.search(full_exp):
        d["fullExplanation"] = full_exp
    return json.dumps(d, ensure_ascii=False, separators=(",", ":"))


def truncate(s: str, n: int) -> str:
    s = re.sub(r"\s+", " ", s).strip()
    if len(s) <= n:
        return s
    return s[: n - 1] + "…"


def append_common_binary_file_inserts(
    lines: List[str], image_blobs: Dict[str, bytes], source_doc: str
) -> None:
    if not image_blobs:
        return
    lines.append("-- 图片资源：写入 arelore_common.common_binary_file")
    for hx in sorted(image_blobs.keys()):
        data = image_blobs[hx]
        ext_s = json.dumps(
            {"sourceDoc": source_doc, "sha256": hx, "size": len(data)},
            ensure_ascii=False,
            separators=(",", ":"),
        )
        hexdata = "0x" + data.hex()
        lines.append("INSERT INTO arelore_common.common_binary_file")
        lines.append("(hash_value, file_data, status, ext_json)")
        lines.append("VALUES")
        lines.append(
            f"    (UNHEX('{hx}'), {hexdata}, 1, '{sql_escape(ext_s)}')"
        )
        lines.append("ON DUPLICATE KEY UPDATE")
        lines.append("                     modify_time = CURRENT_TIMESTAMP,")
        lines.append("                     status = VALUES(status),")
        lines.append("                     ext_json = VALUES(ext_json);")
        lines.append("")


def emit_sql(
    *,
    type_code: str,
    type_name: str,
    type_desc: str,
    meta: dict,
    questions: List[dict],
    image_blobs: Dict[str, bytes],
    source_doc: str,
    out_path: Path,
) -> None:
    max_score = len(questions)
    extra_type = {
        "source": meta.get("source", "答案详解docx"),
        "exam": meta.get("exam", "系统架构设计师"),
        "year": meta.get("year"),
        "term": meta.get("term"),
        "session": meta.get("session", "综合知识"),
        "questionCount": max_score,
        "scoring": {
            "resultMode": "objective_sum",
            "scoreDimension": "score",
            "maxScore": max_score,
            "scoringVersion": "1.0",
        },
        "resultMeanings": {},
    }
    lines: List[str] = []
    lines.append("SET NAMES utf8mb4;")
    lines.append("")
    append_common_binary_file_inserts(lines, image_blobs, source_doc)
    lines.append("USE arelore_education;")
    lines.append("")
    lines.append(
        "INSERT INTO arelore_education.user_detection_type\n"
        "(type_code, type_name, type_description, extra_info)\n"
        "VALUES\n"
        f"    ('{sql_escape(type_code)}', '{sql_escape(type_name)}', '{sql_escape(type_desc)}', "
        f"'{sql_escape(json.dumps(extra_type, ensure_ascii=False, separators=(',', ':')))}')\n"
        "ON DUPLICATE KEY UPDATE\n"
        "                     type_name = VALUES(type_name),\n"
        "                     type_description = VALUES(type_description),\n"
        "                     extra_info = VALUES(extra_info);"
    )
    lines.append("")

    for q in questions:
        qc = f"{type_code}_Q{q['order']:03d}"
        full_stem = strip_question_noise(q["stem"].strip())
        full_exp = strip_question_noise(q["explanation"].strip())
        qn = truncate_avoid_img_token(full_stem, 255) or f"第{q['order']}题"
        opts_json = build_options_json(q)
        extra = build_extra_info(q, full_stem, full_exp)
        lines.append(
            "INSERT INTO arelore_education.user_detection_question\n"
            "(type_code, question_code, question_name, question_order, question_description, options, extra_info)\n"
            "VALUES\n"
            f"    ('{sql_escape(type_code)}', '{sql_escape(qc)}', '{sql_escape(qn)}', {q['order']}, "
            f"'单选题', '{sql_escape(opts_json)}', '{sql_escape(extra)}')\n"
            "ON DUPLICATE KEY UPDATE\n"
            "                     question_name = VALUES(question_name),\n"
            "                     question_order = VALUES(question_order),\n"
            "                     question_description = VALUES(question_description),\n"
            "                     options = VALUES(options),\n"
            "                     extra_info = VALUES(extra_info);"
        )
        lines.append("")

    lines.append(f"-- 总题数: {max_score}")
    out_path.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    base = Path(__file__).resolve().parent.parent.parent / "sql" / "arelore_education" / "软考" / "系统架构设计师"
    files = sorted(base.glob("*答案详解*.docx"))
    if not files:
        print("未找到 docx", file=sys.stderr)
        sys.exit(1)

    for doc in files:
        m = re.match(r"(\d{4})年(.+?)半年", doc.name)
        year = m.group(1) if m else "XXXX"
        term_cn = m.group(2) if m else ""
        term = "H1" if term_cn == "上" else "H2" if term_cn == "下" else "HX"
        type_code = f"RK_SA_ARCH_{year}{term}_GK"
        type_name = f"软考-系统架构设计师-{year}年{term_cn}半年-综合知识"
        type_desc = (
            f"全国计算机技术与软件专业技术资格（水平）考试 系统架构设计师 {year}年{term_cn}半年 "
            f"综合知识试题与解析"
        )

        text, image_blobs = docx_story_text_with_images(doc)
        qs = extract_questions(text)
        if not qs:
            print(f"WARN 无题目: {doc.name}", file=sys.stderr)
            continue
        meta = {
            "source": doc.name,
            "exam": "系统架构设计师",
            "year": year,
            "term": term_cn + "半年",
            "session": "综合知识",
        }
        out_name = f"软考_系统架构设计师_{year}{term_cn}半年_综合知识_导入.sql"
        out_path = base / out_name
        emit_sql(
            type_code=type_code,
            type_name=type_name,
            type_desc=type_desc,
            meta=meta,
            questions=qs,
            image_blobs=image_blobs,
            source_doc=doc.name,
            out_path=out_path,
        )
        print(out_path, len(qs))


if __name__ == "__main__":
    main()
