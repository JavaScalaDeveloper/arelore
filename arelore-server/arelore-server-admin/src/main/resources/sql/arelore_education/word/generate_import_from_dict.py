#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 temp/dict 的 bookLists + book/*.json 生成导入 SQL（admin_word_book / admin_word_entry）。

用法（在仓库根目录）:
  python3 arelore-server/arelore-server-admin/src/main/resources/sql/arelore_education/word/generate_import_from_dict.py

生成目录:
  .../word/import_from_dict/
    00_readme.txt
    01_books.sql
    02_entries_*.sql   # 按词本分文件，便于分批执行

依赖: 已解压 book/*.json；先执行 admin_word.sql 建表与分类种子。
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[8]
DICT_DIR = REPO / "temp" / "dict"
BOOK_DIR = DICT_DIR / "book"
BOOK_LISTS = DICT_DIR / "bookLists.txt"
OUT_DIR = Path(__file__).resolve().parent / "import_from_dict"

CODE_RE = re.compile(r"^[A-Z0-9_]+$")
BATCH = 200


def sql_str(value: str | None) -> str:
    if value is None:
        return "NULL"
    s = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{s}'"


def to_code(raw: str, field: str) -> str:
    code = re.sub(r"[^A-Za-z0-9_]", "_", (raw or "").strip()).upper()
    code = re.sub(r"_+", "_", code).strip("_")
    if not code or not CODE_RE.match(code):
        raise ValueError(f"非法 {field}: {raw!r} -> {code!r}")
    return code


def truncate(s: str, max_chars: int) -> str:
    s = (s or "").strip()
    return s if len(s) <= max_chars else s[: max_chars - 1] + "…"


def category_code(book_id: str) -> str:
    bid = book_id.upper()
    if bid.startswith("PEPXIAOXUE"):
        return "PRIMARY"
    if bid.startswith(("CHUZHONG", "PEPCHUZHONG", "WAIYANSHECHUZHONG")):
        return "JUNIOR"
    if bid.startswith(("GAOZHONG", "PEPGAOZHONG", "BEISHIGAOZHONG")):
        return "SENIOR"
    if bid.startswith("KAOYAN"):
        return "KAOYAN"
    if bid.startswith(("IELTS", "TOEFL", "GRE", "SAT", "GMAT")):
        return "OVERSEAS"
    if bid.startswith(("CET4", "CET6", "LEVEL4", "LEVEL8", "BEC")):
        return "UNIVERSITY"
    raise ValueError(f"无法映射分类: {book_id}")


def load_ndjson(path: Path) -> list[dict]:
    rows = []
    with path.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def main() -> int:
    if not BOOK_LISTS.exists() or not BOOK_DIR.exists():
        print(f"找不到 dict 数据: {DICT_DIR}", file=sys.stderr)
        return 1

    meta_list = json.loads(BOOK_LISTS.read_text(encoding="utf-8"))["data"]["normalBooksInfo"]
    meta = {b["id"]: b for b in meta_list}
    json_files = sorted(BOOK_DIR.glob("*.json"))
    if not json_files:
        print("book/ 下没有 json，请先解压 zip", file=sys.stderr)
        return 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("*.sql"):
        old.unlink()

    readme = OUT_DIR / "00_readme.txt"
    readme.write_text(
        "\n".join(
            [
                "导入顺序:",
                "1) 执行 ../admin_word.sql（建表 + language/category 种子）",
                "2) 执行 01_books.sql",
                "3) 按需批量执行 02_entries_*.sql",
                "",
                "重新生成: python3 generate_import_from_dict.py",
                "",
            ]
        ),
        encoding="utf-8",
    )

    books_sql = [
        "USE arelore_education;",
        "",
        "-- 来自 temp/dict/bookLists.txt + book/*.json",
        "-- 可重复执行：按 code 覆盖更新",
        "",
    ]

    entry_files: list[str] = []
    total_entries = 0
    cat_stats: dict[str, int] = {}

    for path in json_files:
        book_id = path.stem
        if book_id not in meta:
            print(f"跳过无元数据词本: {book_id}", file=sys.stderr)
            continue
        info = meta[book_id]
        book_code = to_code(book_id, "book_code")
        cat = category_code(book_id)
        cat_stats[cat] = cat_stats.get(cat, 0) + 1
        words = load_ndjson(path)
        word_count = int(info.get("wordNum") or len(words))

        book_ext = {
            "sourceId": book_id,
            "cover": info.get("cover"),
            "introduce": info.get("introduce"),
            "version": info.get("version"),
            "size": info.get("size"),
            "reciteUserNum": info.get("reciteUserNum"),
            "bookOrigin": info.get("bookOrigin"),
            "tags": [t.get("tagName") for t in (info.get("tags") or [])],
            "offlinedata": info.get("offlinedata"),
        }
        name = truncate(info.get("title") or book_id, 128)
        desc = truncate(info.get("introduce") or "", 255)

        books_sql.append(
            "INSERT INTO `admin_word_book` "
            "(`code`,`name`,`description`,`word_count`,`language_code`,`category_code`,`status`,`ext_info`) VALUES ("
            f"{sql_str(book_code)},{sql_str(name)},{sql_str(desc)},{word_count},"
            f"'EN',{sql_str(cat)},1,{sql_str(json.dumps(book_ext, ensure_ascii=False))}"
            ") ON DUPLICATE KEY UPDATE "
            "`name`=VALUES(`name`),`description`=VALUES(`description`),`word_count`=VALUES(`word_count`),"
            "`language_code`=VALUES(`language_code`),`category_code`=VALUES(`category_code`),"
            "`status`=VALUES(`status`),`ext_info`=VALUES(`ext_info`);"
        )

        entry_path = OUT_DIR / f"02_entries_{book_code}.sql"
        lines = [
            "USE arelore_education;",
            "",
            f"-- book={book_code} words={len(words)}",
            f"DELETE FROM `admin_word_entry` WHERE `book_code` = {sql_str(book_code)};",
            "",
        ]
        batch: list[str] = []
        for w in words:
            head = (w.get("headWord") or "").strip()
            if not head:
                continue
            word_obj = ((w.get("content") or {}).get("word") or {})
            word_id = word_obj.get("wordId") or f"{book_id}_{w.get('wordRank')}"
            word_code = to_code(str(word_id), "word_code")
            sort_no = int(w.get("wordRank") or 0)
            content = word_obj.get("content") or {}
            ext = {
                "wordRank": w.get("wordRank"),
                "sourceWordId": word_id,
                **content,
            }
            word_col = truncate(head, 128)
            ext_json = json.dumps(ext, ensure_ascii=False)
            batch.append(
                f"({sql_str(book_code)},{sql_str(word_code)},{sql_str(word_col)},{sort_no},{sql_str(ext_json)})"
            )
            if len(batch) >= BATCH:
                lines.append(
                    "INSERT INTO `admin_word_entry` "
                    "(`book_code`,`word_code`,`word`,`sort_no`,`ext_info`) VALUES\n"
                    + ",\n".join(batch)
                    + ";"
                )
                lines.append("")
                total_entries += len(batch)
                batch = []
        if batch:
            lines.append(
                "INSERT INTO `admin_word_entry` "
                "(`book_code`,`word_code`,`word`,`sort_no`,`ext_info`) VALUES\n"
                + ",\n".join(batch)
                + ";"
            )
            lines.append("")
            total_entries += len(batch)

        entry_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        entry_files.append(entry_path.name)
        print(f"OK {book_code} -> {cat} entries={len(words)}")

    (OUT_DIR / "01_books.sql").write_text("\n".join(books_sql) + "\n", encoding="utf-8")
    print("---")
    print(f"books={len(entry_files)} entries={total_entries}")
    print("categories:", cat_stats)
    print(f"output: {OUT_DIR}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
