#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
本仓库示例：从固定相对路径下的「答案详解」类 docx 抽取客观题并写 SQL。
逻辑见 ruankao_doc_parse.py；输入/输出目录写死在脚本内。其它资格请使用 generate_generic_docx_sql.py。
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

from ruankao_doc_parse import docx_story_text_with_images, emit_sql, extract_questions


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
