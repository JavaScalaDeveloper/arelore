#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
软考客观题 docx 正文解析与 SQL 生成公共逻辑（与 `generate_sysarch_sql.py`、`generate_generic_docx_sql.py` 共用）。
"""
from __future__ import annotations

import hashlib
import html
import json
import re
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
A_NS = "http://schemas.openxmlformats.org/drawingml/2006/main"

IMG_PLACEHOLDER = re.compile(r"【img:([0-9a-f]{64})】")
UNOFFICIAL_SUFFIX = "（非官方）"


def append_unofficial_suffix(type_name: str) -> str:
    if not type_name or type_name.endswith(UNOFFICIAL_SUFFIX):
        return type_name
    return type_name + UNOFFICIAL_SUFFIX


def infer_unofficial_exam(
    filename: str,
    text: str,
    question_count: int,
    session_suffix: str,
    *,
    objective_count: int = 0,
) -> bool:
    """
    识别网络题库/精简卷等非官方软考 docx。
    命中时 type_name 应追加「（非官方）」。
    """
    if question_count <= 0:
        return False
    n = filename
    name_markers = ("最新", "试题与答案", "真题试题", "考试试题", "年全国软考")
    if any(m in n for m in name_markers):
        return True
    if re.search(r"每题\d+分，共20分", text) or re.search(r"每题2分，共20分", text):
        return True
    if re.search(r"(?m)^###\s*[一二三四五六七八九十]", text) or "考试时间：" in text:
        return True
    if session_suffix in ("JC", "JJ", "GK") and question_count < 60:
        return True
    if (
        session_suffix == "YY"
        and objective_count == question_count
        and question_count >= 10
        and question_count < 50
    ):
        return True
    return False


def strip_question_noise(s: str) -> str:
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
    s = s.replace("【参考答案】", "").replace("【答案】", "").strip()
    s = re.sub(r"\s+", " ", s)
    return s.strip()


ANSWER_MARKER_RE = re.compile(r"【参考答案】|【答案】")


OPTION_LINE_RE = re.compile(r"^[\s∙·•\*]*([A-G])[\.．、]\s*(.+)$", re.I)


def split_multi_answers(ans: str) -> List[str]:
    s = ans.strip()
    parts = re.split(r"[\s、,，]+", s)
    letters = [p.strip().upper() for p in parts if p.strip() and len(p.strip()) == 1]
    if letters:
        return letters
    if re.fullmatch(r"[A-G]+", s, re.I):
        return list(s.upper())
    return normalize_judgment_answer(s)


def normalize_judgment_answer(raw: str) -> List[str]:
    s = raw.strip()
    if s in ("√", "对", "正确", "T", "Y", "是"):
        return ["A"]
    if s in ("×", "✗", "错", "错误", "F", "N", "否", "B"):
        return ["B"]
    return []


def parse_options_one_line(line: str) -> Optional[Tuple[int, Dict[str, str]]]:
    m = re.match(r"^\((\d+)\)\s*A[\.．、]\s*(.+)$", line.strip(), re.I)
    if not m:
        return None
    num = int(m.group(1))
    rest = m.group(2).strip()
    one = re.sub(r"\s+", " ", "A." + rest)
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


def extract_questions_colon_answer(text: str) -> List[dict]:
    """
    版式：题号「N、」题干；选项行「A. …」；「答案：X」；解析为答案行后至下一题前的正文。
    无【答案】/【解析】标记时使用。
    """
    text = normalize_exam_text(text)
    out: List[dict] = []
    chunks = list(re.finditer(r"(?m)^(\d+)[、．.]\s*", text))
    if not chunks:
        return []

    for i, m in enumerate(chunks):
        num = int(m.group(1))
        start = m.end()
        end = chunks[i + 1].start() if i + 1 < len(chunks) else len(text)
        block = text[start:end].strip()
        ans_m = re.search(r"答案\s*[：:]\s*([^\n]+)", block, re.I)
        if not ans_m:
            continue
        letters = split_multi_answers(ans_m.group(1))
        if not letters:
            continue
        before_ans = block[: ans_m.start()].strip()
        explanation = strip_question_noise(block[ans_m.end() :].strip())
        parsed = _parse_before_answer_block(before_ans, num)
        if not parsed:
            continue
        qtype = parsed.get("question_type") or (
            "多选题" if len(letters) > 1 else "单选题"
        )
        out.append(
            {
                "order": num,
                "stem": parsed["stem"],
                "options": parsed["options"],
                "answer_keys": letters,
                "explanation": explanation,
                "question_type": qtype,
            }
        )

    seen = set()
    uniq: List[dict] = []
    for q in sorted(out, key=lambda x: x["order"]):
        if q["order"] in seen:
            continue
        seen.add(q["order"])
        uniq.append(q)
    return uniq


def _split_explanation_region(raw: str) -> Tuple[str, int]:
    """从「解析：」后至下一「答案：」前的片段中拆出解析正文与下一题起始偏移。"""
    if not raw.strip():
        return "", 0
    lines = raw.split("\n")
    exp_lines: List[str] = []
    i = 0
    while i < len(lines):
        ln = lines[i].strip()
        if not ln:
            i += 1
            continue
        if re.match(r"^\d+\.\s", ln):
            break
        rest = "\n".join(lines[i:])
        if i > 0 and re.search(r"(?m)^[\s∙·•\*]*[A-G][\.．、]\s", rest):
            break
        exp_lines.append(lines[i])
        i += 1
    explanation = "\n".join(exp_lines).strip()
    if i >= len(lines):
        return explanation, len(raw)
    consumed = sum(len(lines[j]) + 1 for j in range(i))
    return explanation, consumed


def _strip_exam_preamble(block: str) -> str:
    lines = block.split("\n")
    out: List[str] = []
    for ln in lines:
        s = ln.strip()
        if not s:
            continue
        if re.match(r"^#{1,3}\s", s):
            continue
        if re.search(r"考试时间|总分|班级|_______", s):
            continue
        if re.match(r"^\d{4}年.*(?:考试|真题|试题)", s):
            continue
        out.append(ln)
    return "\n".join(out).strip()


def _parse_judgment_block(before_ans: str, order: int) -> Optional[dict]:
    block = strip_question_noise(_strip_exam_preamble(before_ans))
    if not block or not re.search(r"[（(]\s*[）)]\s*$", block):
        return None
    lines = [ln.strip() for ln in block.split("\n") if ln.strip()]
    stem_parts: List[str] = []
    for ln in lines:
        if re.match(r"^(?:#{1,3}\s*)?[一二三四五六七八九十]+[、．.]", ln):
            continue
        nm = re.match(r"^(\d+)\.\s*(.+)$", ln)
        if nm:
            stem_parts.append(nm.group(2).strip())
        elif not re.match(r"^[①②③④⑤⑥⑦⑧]\s*$", ln):
            stem_parts.append(ln)
    stem = strip_question_noise(" ".join(stem_parts))
    if not stem:
        return None
    return {
        "order": order,
        "stem": stem,
        "options": {"A": "正确", "B": "错误"},
        "question_type": "判断题",
    }


def _parse_before_answer_block(before_ans: str, order: int) -> Optional[dict]:
    """从「答案：」前的正文解析题干与选项。"""
    block = strip_question_noise(_strip_exam_preamble(before_ans))
    if not block:
        return None
    lines = [ln.strip() for ln in block.split("\n") if ln.strip()]
    opt_lines: List[Tuple[str, str]] = []
    stem_parts: List[str] = []
    for ln in lines:
        om = OPTION_LINE_RE.match(ln)
        if om:
            opt_lines.append((om.group(1).upper(), strip_question_noise(om.group(2))))
        elif opt_lines and not re.match(r"^[①②③④⑤⑥⑦⑧]\s*$", ln):
            k, v = opt_lines[-1]
            opt_lines[-1] = (k, f"{v} {strip_question_noise(ln)}".strip())
        elif not re.match(r"^[①②③④⑤⑥⑦⑧]\s*$", ln):
            if re.match(r"^(?:#{1,3}\s*)?[一二三四五六七八九十]+[、．.]", ln):
                continue
            nm = re.match(r"^(\d+)\.\s*(.+)$", ln)
            if nm:
                stem_parts.append(nm.group(2).strip())
            else:
                stem_parts.append(ln)

    if len(opt_lines) < 2 and len(lines) >= 3:
        k = min(4, len(lines))
        if len(lines) > k:
            stem_parts = lines[:-k]
            opt_lines = []
            for j in range(k):
                om = OPTION_LINE_RE.match(lines[-k + j].strip())
                if om:
                    opt_lines.append((om.group(1).upper(), strip_question_noise(om.group(2))))

    if len(opt_lines) < 2:
        return _parse_judgment_block(before_ans, order)

    opts_map = {k: v for k, v in opt_lines}
    stem = strip_question_noise(" ".join(stem_parts))
    if not stem and stem_parts:
        stem = strip_question_noise(stem_parts[0])
    if not stem:
        stem = f"第{order}题"
    return {"order": order, "stem": stem, "options": opts_map}


def extract_questions_answer_jixi(text: str) -> List[dict]:
    """
    版式：可选「N.」题干；选项「A. …」；「答案：X」；「解析：…」。
    常见于网络题库 docx（含无题号小题）。
    """
    text = normalize_exam_text(text)
    start_m = re.search(r"(?m)^(?:#{1,3}\s*)?一[、．.]", text)
    if start_m:
        text = text[start_m.start() :]

    answer_marks = list(re.finditer(r"(?m)\n\s*答案\s*[：:]\s*([^\n]+)", text))
    if len(answer_marks) < 2:
        return []

    out: List[dict] = []
    cursor = 0
    for i, am in enumerate(answer_marks):
        before = _strip_exam_preamble(text[cursor : am.start()].strip())
        letters = split_multi_answers(am.group(1))
        if not letters:
            continue
        exp_hdr = re.match(r"\s*\n\s*解析\s*[：:]\s*", text[am.end() :])
        if not exp_hdr:
            continue
        exp_start = am.end() + exp_hdr.end()
        exp_end = answer_marks[i + 1].start() if i + 1 < len(answer_marks) else len(text)
        raw_exp = text[exp_start:exp_end]
        explanation, consumed = _split_explanation_region(raw_exp)
        explanation = strip_question_noise(explanation)
        sec = re.search(
            r"(?m)\n(?:#{1,3}\s*)?[一二三四五六七八九十]+[、．.]", raw_exp[consumed:]
        )
        if sec:
            cursor = exp_start + consumed + sec.end()
        else:
            cursor = exp_start + consumed

        nums = list(re.finditer(r"(?m)^(\d+)\.\s*", before))
        src_no = int(nums[-1].group(1)) if nums else len(out) + 1
        parsed = _parse_before_answer_block(before, src_no)
        if not parsed:
            continue
        qtype = parsed.get("question_type") or (
            "多选题" if len(letters) > 1 else "单选题"
        )
        out.append(
            {
                "order": len(out) + 1,
                "source_no": src_no,
                "stem": parsed["stem"],
                "options": parsed["options"],
                "answer_keys": letters,
                "explanation": explanation,
                "question_type": qtype,
            }
        )
    return out


def normalize_exam_text(text: str) -> str:
    """统一题号括号等，便于 (n)A. 与【答案】版式匹配。"""
    text = text.replace("\r", "")
    text = text.replace("（", "(").replace("）", ")")
    return text


def _parse_options_multiline(block: str) -> Dict[str, str]:
    """从题干块中解析 A. / A． 选项（支持多行）。"""
    marks = list(re.finditer(r"(?m)^\s*([A-G])[．.、]\s*", block))
    if len(marks) < 2:
        one_line = re.sub(r"\s+", " ", block.replace("\n", " "))
        marks = list(re.finditer(r"([A-G])[．.、]\s*", one_line))
        if len(marks) < 2:
            return {}
        found: Dict[str, str] = {}
        for i, km in enumerate(marks):
            k = km.group(1).upper()
            start = km.end()
            end = marks[i + 1].start() if i + 1 < len(marks) else len(one_line)
            found[k] = strip_question_noise(one_line[start:end].strip())
        return found

    found = {}
    for i, km in enumerate(marks):
        k = km.group(1).upper()
        start = km.end()
        end = marks[i + 1].start() if i + 1 < len(marks) else len(block)
        found[k] = strip_question_noise(block[start:end].strip())
    return found


def _split_db_answer_section(text: str) -> Tuple[str, str]:
    m = re.search(r"(?m)^答案\s*[:：]\s*$", text)
    if m:
        return text[: m.start()].strip(), text[m.end() :].strip()
    m2 = re.search(r"(?m)^参考答案\s*$", text)
    if m2:
        return text[: m2.start()].strip(), text[m2.end() :].strip()
    m3 = re.search(r"(?m)^答案\s*[:：]\s*\n", text)
    if m3:
        return text[: m3.start()].strip(), text[m3.end() :].strip()
    return text, ""


def _parse_db_answer_keys_and_explanations(ans_text: str) -> Tuple[Dict[int, str], Dict[int, str]]:
    keys: Dict[int, str] = {}
    explanations: Dict[int, str] = {}
    if not ans_text.strip():
        return keys, explanations

    for m in re.finditer(
        r"(\d+[、．]\s*[A-G](?:\s+\d+[、．]\s*[A-G])*)\s*\n\[解析\](.*?)(?=\n\d+[、．]\s*[A-G]|\Z)",
        ans_text,
        re.S,
    ):
        nums = [int(x) for x in re.findall(r"(\d+)[、．]", m.group(1))]
        exp = strip_question_noise(m.group(2).strip())
        for n in nums:
            explanations[n] = exp

    for m in re.finditer(r"(\d+)[、．]\s*([A-G])\b", ans_text):
        keys[int(m.group(1))] = m.group(2).upper()

    for m in re.finditer(
        r"(\d+)\s*[-~～]\s*(\d+)\s*[：:]\s*([A-G][A-G\s]*)", ans_text, re.I
    ):
        start_n, end_n = int(m.group(1)), int(m.group(2))
        letters = [c.upper() for c in re.findall(r"[A-G]", m.group(3))]
        for offset, num in enumerate(range(start_n, end_n + 1)):
            if offset < len(letters):
                keys[num] = letters[offset]

    return keys, explanations


def extract_questions_db_numbered(text: str) -> List[dict]:
    """
    版式：正文「N、」题干 + A．选项；文末「答案:」+「N、X」+「[解析]」。
    常见于数据库系统工程师等历年真题 Word。
    """
    body, ans = _split_db_answer_section(text)
    if not ans.strip():
        return []

    answer_keys, explanations = _parse_db_answer_keys_and_explanations(ans)
    if not answer_keys:
        return []

    out: List[dict] = []
    chunks = list(re.finditer(r"(?m)^(\d+)[、．]\s*", body))
    for i, m in enumerate(chunks):
        num = int(m.group(1))
        start = m.end()
        end = chunks[i + 1].start() if i + 1 < len(chunks) else len(body)
        block = body[start:end].strip()
        if num not in answer_keys:
            continue
        opts_map = _parse_options_multiline(block)
        if len(opts_map) < 2:
            continue
        stem_m = re.match(r"^(.*?)(?=\n\s*[A-G][．.、]|\s+[A-G][．.、])", block, re.S)
        stem = strip_question_noise((stem_m.group(1) if stem_m else block).strip())
        if not stem:
            stem = f"第{num}题"
        out.append(
            {
                "order": num,
                "stem": stem,
                "options": opts_map,
                "answer_keys": [answer_keys[num]],
                "explanation": explanations.get(num, ""),
                "question_type": "单选题",
            }
        )

    return _dedupe_questions(out)


def extract_questions_jieda_style(text: str) -> List[dict]:
    """
    版式：「01.」题干 + A. 选项 +「解答：答案正确/选择 X」+ 解析正文。
    常见于软件评测师等整理版真题。
    """
    text = normalize_exam_text(text)
    out: List[dict] = []
    pattern = re.compile(
        r"(?ms)(?:【[^】]*第\s*(\d+)\s*题[^】]*】\s*)?"
        r"(\d{1,2})[\.．、]\s*(.*?)"
        r"(?=\n(?:【[^】]*第\s*\d+\s*题|\d{1,2}[\.．、]\s*)|\Z)"
    )
    for m in pattern.finditer(text):
        src_no = int(m.group(1) or m.group(2))
        block = m.group(3).strip()
        ans_m = re.search(
            r"解答\s*[：:]\s*(?:答案)?(?:正确|选择|为)?\s*([A-G])\b", block, re.I
        )
        if not ans_m:
            continue
        letter = ans_m.group(1).upper()
        before = block[: ans_m.start()].strip()
        after = strip_question_noise(block[ans_m.end() :].strip())
        after = re.sub(r"^[。．.\s]+", "", after)

        opts_map = _parse_options_multiline(before)
        if len(opts_map) < 2:
            continue
        stem_m = re.match(
            r"^(.*?)(?=\n\s*[A-G][\.．、]|\n[A-G][\.．、]|\s+[A-G][\.．、])",
            before,
            re.S,
        )
        stem = strip_question_noise((stem_m.group(1) if stem_m else before).strip())
        if not stem:
            stem = f"第{src_no}题"

        out.append(
            {
                "order": src_no,
                "stem": stem,
                "options": opts_map,
                "answer_keys": [letter],
                "explanation": after,
                "question_type": "单选题",
            }
        )

    return _dedupe_questions(out)


def extract_questions_shiti_inline_answer(text: str) -> List[dict]:
    """
    版式：「试题N」题干 + A. 选项 + 行内「答案：X」+ 可选解析。
    常见于电子商务设计师等上午真题。
    """
    text = normalize_exam_text(text)
    out: List[dict] = []
    pat = re.compile(r"(?ms)试题\s*(\d+)\s*(.*?)(?=试题\s*\d+|\Z)")
    for m in pat.finditer(text):
        num = int(m.group(1))
        block = m.group(2).strip()
        ans_m = re.search(r"答案\s*[：:]\s*([A-G])\b", block, re.I)
        if not ans_m:
            continue
        letter = ans_m.group(1).upper()
        before = block[: ans_m.start()].strip()
        after = strip_question_noise(block[ans_m.end() :].strip())
        after = re.sub(r"^[。．.\s]+", "", after)
        opts_map = _parse_options_multiline(before)
        if len(opts_map) < 2:
            continue
        stem_m = re.match(
            r"^(.*?)(?=\n\s*[A-G][\.．、]|\n[A-G][\.．、]|\s+[A-G][\.．、])",
            before,
            re.S,
        )
        stem = strip_question_noise((stem_m.group(1) if stem_m else before).strip())
        if not stem:
            stem = f"第{num}题"
        out.append(
            {
                "order": num,
                "stem": stem,
                "options": opts_map,
                "answer_keys": [letter],
                "explanation": after,
                "question_type": "单选题",
            }
        )
    return _dedupe_questions(out)


def extract_questions_bullet_answer(text: str) -> List[dict]:
    """
    版式：「●」题干 +「（n）A.」选项 +「答案：X」+「解析：」。
    常见于 2018+ 整理版真题 docx。
    """
    text = normalize_exam_text(text)
    out: List[dict] = []
    blocks = re.split(r"(?=●\s*)", text)
    for block in blocks:
        block = block.strip()
        if not block or not re.search(r"答案\s*[：:]", block):
            continue
        ans_m = re.search(r"答案\s*[：:]\s*([A-G])\b", block)
        if not ans_m:
            continue
        letter = ans_m.group(1).upper()
        before = re.sub(r"^●\s*", "", block[: ans_m.start()].strip())
        after_ans = block[ans_m.end() :]
        exp_m = re.search(r"解析\s*[：:]", after_ans)
        explanation = ""
        if exp_m:
            exp_body = after_ans[exp_m.end() :]
            nxt = re.search(r"(?=●\s*)", exp_body)
            explanation = strip_question_noise(
                (exp_body[: nxt.start()] if nxt else exp_body).strip()
            )

        num_m = re.search(r"[（(](\d+)[）)]", before[:300])
        order = int(num_m.group(1)) if num_m else len(out) + 1

        opts_map = _parse_options_multiline(before)
        if len(opts_map) < 2:
            one = re.sub(r"\s+", " ", before.replace("\n", " "))
            marks = list(re.finditer(r"([A-G])[．.、]\s*", one))
            if len(marks) >= 2:
                for i, km in enumerate(marks):
                    k = km.group(1).upper()
                    start = km.end()
                    end = marks[i + 1].start() if i + 1 < len(marks) else len(one)
                    opts_map[k] = strip_question_noise(one[start:end].strip())

        if len(opts_map) < 2:
            continue

        stem_m = re.match(
            r"^(.*?)(?=[（(]\d+[）)]\s*[A-G][．.、]|[A-G][．.、])",
            before,
            re.S,
        )
        stem = strip_question_noise((stem_m.group(1) if stem_m else before).strip())
        if not stem:
            stem = f"第{order}题"

        out.append(
            {
                "order": order,
                "stem": stem,
                "options": opts_map,
                "answer_keys": [letter],
                "explanation": explanation,
                "question_type": "单选题",
            }
        )

    return _dedupe_questions(out)


def _find_subjective_question_body_end(text: str) -> int:
    """定位下午卷参考答案区起始位置，避免把答案块当成新大题。"""
    markers = [
        r"(?m)^试题一\s*\n\s*\d+[、．]\s*起点",
        r"(?m)^参考答案",
        r"(?m)^\d+、[【]?问题\d+】?解答",
    ]
    end = len(text)
    for pat in markers:
        m = re.search(pat, text)
        if m and m.start() < end:
            end = m.start()
    return end


def extract_subjective_shiti(text: str) -> List[dict]:
    """
    下午案例分析：按「试题一 / 试题1」分块，无 A/B/C/D 选项。
    """
    text = normalize_exam_text(text)
    cn = "一二三四五六七八九十"
    body_end = _find_subjective_question_body_end(text)
    body = text[:body_end]
    patterns = [
        re.compile(rf"试题\s*([{cn}]+|\d+)\s*[（(]\s*(?:共|约)?\s*\d+\s*分"),
        re.compile(rf"(?m)^试题\s*([{cn}\d]+)\s*$"),
    ]
    matches: List[re.Match] = []
    for pat in patterns:
        ms = list(pat.finditer(body))
        if len(ms) >= 2:
            matches = ms
            break
        if len(ms) == 1 and len(body) > 400:
            matches = ms
            break
    if not matches:
        return []

    cn_map = {c: i + 1 for i, c in enumerate(cn)}

    def _shiti_order(label: str) -> int:
        label = label.strip()
        if label.isdigit():
            return int(label)
        if len(label) == 1 and label in cn_map:
            return cn_map[label]
        total = 0
        for ch in label:
            if ch in cn_map:
                total = total * 10 + cn_map[ch]
        return total or 1

    out: List[dict] = []
    seen_orders: set = set()
    for i, m in enumerate(matches):
        order = _shiti_order(m.group(1))
        if order in seen_orders:
            continue
        seen_orders.add(order)
        start = m.start()
        end = matches[i + 1].start() if i + 1 < len(matches) else len(body)
        stem = strip_answer_sections_from_stem(
            strip_question_noise(body[start:end].strip()), subjective=True
        )
        if len(stem) < 20:
            continue
        if re.match(r"^\d+[、．]\s*起点", stem):
            continue
        out.append(
            {
                "order": order,
                "stem": stem,
                "options": {},
                "answer_keys": [],
                "explanation": "",
                "question_type": "案例分析",
            }
        )

    return _dedupe_questions(out)


def _dedupe_questions(out: List[dict]) -> List[dict]:
    seen = set()
    uniq: List[dict] = []
    for q in sorted(out, key=lambda x: x["order"]):
        if q["order"] in seen:
            continue
        seen.add(q["order"])
        uniq.append(q)
    return uniq


def question_has_options(q: dict) -> bool:
    return bool(q.get("options"))


def strip_answer_sections_from_stem(stem: str, *, subjective: bool = False) -> str:
    """从题干中移除答案、解析等不应在刷题时展示的内容。"""
    if not stem:
        return stem
    stem = strip_question_noise(stem)
    patterns = [
        r"\[解析\]",
        r"【解析】",
        r"(?m)^参考答案",
        r"(?m)^答案\s*[:：]\s*$",
    ]
    if subjective:
        patterns.extend(
            [
                r"(?m)^\d+[、．]\s*起点[：:]",
                r"(?m)^\d+[、．]\s*终点[：:]",
                r"(?m)^\d+[、．]\s*[A-G]\s+",
                r"(?m)^答案\s*[:：]",
            ]
        )
    for pat in patterns:
        m = re.search(pat, stem)
        if not m:
            continue
        min_pos = 8 if subjective else 40
        if m.start() >= min_pos:
            stem = stem[: m.start()].strip()
    if subjective:
        for head in (r"阅读下列", r"【说明】", r"【问题", r"\[问题", r"(?m)^说明\s*$"):
            m = re.search(head, stem)
            if m:
                return stem[m.start() :].strip()
    return stem


def subjective_short_title(stem: str, order: int) -> str:
    m = re.match(r"^(试题\s*[一二三四五六七八九十\d]+)", stem)
    if m:
        return m.group(1).strip()
    first = stem.split("\n", 1)[0].strip()
    if first and len(first) <= 80:
        return first
    return f"第{order}题（案例分析）"


def infer_question_type(q: dict) -> str:
    if q.get("question_type"):
        return str(q["question_type"])
    keys = q.get("answer_keys") or []
    if question_has_options(q) and len(keys) > 1:
        return "多选题"
    return "单选题" if question_has_options(q) else "案例分析"


def extract_questions(text: str) -> List[dict]:
    text = normalize_exam_text(text)
    parsers = (
        _extract_questions_bracket_markers,
        extract_questions_db_numbered,
        extract_questions_shiti_inline_answer,
        extract_questions_jieda_style,
        extract_questions_bullet_answer,
        extract_questions_answer_jixi,
        extract_questions_colon_answer,
    )
    best: List[dict] = []
    for fn in parsers:
        try:
            got = fn(text)
        except Exception:
            continue
        if len(got) > len(best):
            best = got
    if best:
        return best
    return extract_subjective_shiti(text)


def _extract_questions_bracket_markers(text: str) -> List[dict]:
    out: List[dict] = []
    cursor = 0
    while True:
        m_ans = ANSWER_MARKER_RE.search(text, cursor)
        if not m_ans:
            break
        p = m_ans.start()
        prev = text[cursor:p]
        matches = list(re.finditer(r"\((\d+)\)\s*A[\.．、]\s*", prev))
        if not matches:
            cursor = m_ans.end()
            continue

        ans_line_end = text.find("\n", p)
        if ans_line_end == -1:
            ans_line_end = len(text)
        ans_raw = text[p:ans_line_end]
        letters = split_multi_answers(clean_answer_line(ans_raw))
        if not letters:
            cursor = m_ans.end()
            continue

        pd = text.find("【解析】", ans_line_end)
        if pd == -1:
            cursor = m_ans.end()
            continue
        exp_start = pd + len("【解析】")
        nex = re.search(r"\n\(\d+\)\s*A[\.．、]\s*", text[exp_start:])
        if nex:
            raw_block = text[exp_start : exp_start + nex.start()]
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

        cursor = max(cursor, exp_start + len(raw_exp) if pd != -1 else m_ans.end())
        while cursor < len(text) and text[cursor] == "\n":
            cursor += 1

    return _dedupe_questions(out)


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def build_options_json(q: dict) -> str:
    if not question_has_options(q):
        return "[]"
    letters = {k.upper() for k in (q.get("answer_keys") or []) if k}
    opts = []
    for key in sorted(q["options"].keys()):
        is_correct = bool(letters) and key.upper() in letters
        opts.append(
            {
                "key": key,
                "text": strip_question_noise(q["options"][key]),
                "scores": {"score": 1 if is_correct else 0},
            }
        )
    return json.dumps(opts, ensure_ascii=False, separators=(",", ":"))


def build_extra_info(q: dict, full_stem: str, *, short_title: str = "") -> str:
    """刷题用 extra_info：题干不含解析；标准答案键仅用于客观题自动判分。"""
    qtype = infer_question_type(q)
    opt_blob = ""
    if question_has_options(q):
        opt_blob = " ".join(
            strip_question_noise(q["options"][k]) for k in sorted(q["options"].keys())
        )
    d: dict = {
        "questionType": qtype,
        "sourceQuestionNo": q["order"],
        "imageHashValues": ordered_image_hashes(full_stem, "", opt_blob),
    }
    keys = [k.upper() for k in (q.get("answer_keys") or []) if k]
    if question_has_options(q) and keys:
        if len(keys) > 1:
            d["correctOptionKeys"] = sorted(keys)
        else:
            d["correctOptionKey"] = keys[0]
    subjective = not question_has_options(q)
    if subjective or len(full_stem) > 255 or IMG_PLACEHOLDER.search(full_stem):
        d["fullQuestionName"] = full_stem
    if short_title and subjective:
        d["displayTitle"] = short_title
    return json.dumps(d, ensure_ascii=False, separators=(",", ":"))


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
        lines.append(f"    (UNHEX('{hx}'), {hexdata}, 1, '{sql_escape(ext_s)}')")
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
    objective_count = sum(1 for q in questions if question_has_options(q))
    subjective_count = len(questions) - objective_count
    max_score = objective_count
    extra_type = {
        "source": meta.get("source", "答案详解docx"),
        "exam": meta.get("exam", "系统架构设计师"),
        "year": meta.get("year"),
        "term": meta.get("term"),
        "session": meta.get("session", "综合知识"),
        "questionCount": len(questions),
        "objectiveCount": objective_count,
        "subjectiveCount": subjective_count,
        "resultMeanings": {},
    }
    if meta.get("unofficial"):
        extra_type["unofficial"] = True
    if objective_count > 0:
        extra_type["scoring"] = {
            "resultMode": "objective_sum",
            "scoreDimension": "score",
            "maxScore": max_score,
            "scoringVersion": "1.0",
        }
    else:
        extra_type["scoring"] = {
            "resultMode": "subjective_review",
            "scoreDimension": "score",
            "maxScore": 0,
            "scoringVersion": "1.0",
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
        subjective = not question_has_options(q)
        full_stem = strip_question_noise(q["stem"].strip())
        full_stem = strip_answer_sections_from_stem(full_stem, subjective=subjective)
        if subjective:
            short = subjective_short_title(full_stem, q["order"])
            qn = short
        else:
            qn = truncate_avoid_img_token(full_stem, 255) or f"第{q['order']}题"
        qtype = infer_question_type(q)
        qdesc = qtype if subjective else "单选题"
        opts_json = build_options_json(q)
        extra = build_extra_info(q, full_stem, short_title=qn if subjective else "")
        lines.append(
            "INSERT INTO arelore_education.user_detection_question\n"
            "(type_code, question_code, question_name, question_order, question_description, options, extra_info)\n"
            "VALUES\n"
            f"    ('{sql_escape(type_code)}', '{sql_escape(qc)}', '{sql_escape(qn)}', {q['order']}, "
            f"'{sql_escape(qdesc)}', '{sql_escape(opts_json)}', '{sql_escape(extra)}')\n"
            "ON DUPLICATE KEY UPDATE\n"
            "                     question_name = VALUES(question_name),\n"
            "                     question_order = VALUES(question_order),\n"
            "                     question_description = VALUES(question_description),\n"
            "                     options = VALUES(options),\n"
            "                     extra_info = VALUES(extra_info);"
        )
        lines.append("")

    lines.append(
        f"-- 总题数: {len(questions)}（客观 {objective_count}，主观/无选项 {subjective_count}）"
    )
    out_path.write_text("\n".join(lines), encoding="utf-8")
