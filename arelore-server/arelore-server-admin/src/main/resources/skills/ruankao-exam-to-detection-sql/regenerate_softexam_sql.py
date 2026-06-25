#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""按科目目录批量重新生成软考 SQL（与 generate_generic_docx_sql 相同参数）。"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
SQL_SOFT = SCRIPT_DIR.parent.parent / "sql" / "arelore_education" / "软考"

# (相对软考根目录, type_prefix, exam_name, exam_level, doc_root 相对该目录或 None 表示同目录)
JOBS = [
    ("中级/数据库系统工程师", "RK_DBENG", "数据库系统工程师", "中级", None),
    ("中级/电子商务设计师", "RK_ECOM", "电子商务设计师", "中级", None),
    ("中级/软件测评师历年真题", "RK_SOFTTEST", "软件评测师", "中级", None),
    ("中级/信息系统管理工程师", "RK_ISMENG", "信息系统管理工程师", "中级", None),
    ("高级/系统分析师", "RK_SYSANAL", "系统分析师", "高级", None),
    ("中级/网络工程师", "RK_NETENG", "网络工程师", "中级", None),
    ("初级/程序员", "RK_PROG", "程序员", "初级", "初级/程序员历年真题"),
    ("初级/网络管理员", "RK_NETADM", "网络管理员", "初级", None),
    ("初级/信息处理技术员", "RK_IPT", "信息处理技术员", "初级", None),
    ("高级/系统架构设计师", "RK_SYSARCH", "系统架构设计师", "高级", None),
]


def main() -> None:
    gen = SCRIPT_DIR / "generate_generic_docx_sql.py"
    for rel, prefix, name, level, doc_sub in JOBS:
        out_dir = SQL_SOFT / rel
        root = SQL_SOFT / doc_sub if doc_sub else out_dir
        if not root.is_dir():
            print(f"SKIP 无目录: {root}", file=sys.stderr)
            continue
        cmd = [
            sys.executable,
            str(gen),
            "--type-prefix",
            prefix,
            "--exam-name",
            name,
            "--exam-level",
            level,
            "--out-dir",
            str(out_dir),
            "--root",
            str(root),
        ]
        if doc_sub:
            cmd.append("--recursive")
        if "程序员" in rel:
            cmd.extend(["--name-substr", "答案"])
        if "网络工程师" in rel:
            cmd.extend(["--name-substr", "答案详解"])
        print("===", name, "===", flush=True)
        subprocess.run(cmd, check=False)


if __name__ == "__main__":
    main()
