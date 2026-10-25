#!/usr/bin/env python3
"""Demo: emit one user_ai_prompt INSERT following seed-en-dialogue-china rules."""

from __future__ import annotations

import hashlib
import json

BATCH_ID = "en-cn-dialogue-china-demo"
USER_ID = "26882207331096889723"

# --- one demo row ---
CATEGORY = "问路"
SUB = "公交车"
PROTAGONIST = "小女孩"
TITLE = "小女孩·公交车英语🚌"  # short, has 英语, no #
SUMMARY = (
    "问路不会开口？站台这一句「Excuse me」先救场。"
    "听感干净，适合反复跟读。"
    "#英语口语 #生活英语"
)  # scene hook + hashtags; no 竖屏/字幕/主角名

PROMPT = """生成要求：9:16竖屏，15-20秒。中国城市路边公交站台，有中文站牌与候车亭。语速明快。风格：真实生活记录风。

画面描述：小女孩（约6-10岁，马尾辫配粉色发卡，浅色短袖连衣裙，活泼自然）正在「问路/公交车」场景中，在「公交车」用英语完成沟通，主角是小女孩。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

小女孩: I'm looking for the bus stop. Is it far? (我在找the bus stop。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

小女孩: Thank you so much. (太谢谢了。)

热心路人: You're welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。"""

CONFIG_TAGS = ["抖音", "视频号", "小红书", "英语", "英语情景对话", CATEGORY, PROTAGONIST]


def sql_str(s: str) -> str:
    return s.replace("'", "''")


def prompt_uuid(batch_id: str, protagonist: str, category: str, sub: str) -> str:
    return hashlib.md5(f"{batch_id}|{protagonist}|{category}|{sub}".encode()).hexdigest()


def main() -> None:
    assert TITLE != SUMMARY and TITLE not in SUMMARY
    assert "英语" in TITLE and "#" not in TITLE
    assert SUMMARY.count("#") >= 2

    uuid = prompt_uuid(BATCH_ID, PROTAGONIST, CATEGORY, SUB)
    ext = {
        "summary": SUMMARY,
        "source": "seed_sql",
        "importBatchId": BATCH_ID,
        "sceneCategory": CATEGORY,
        "sceneSub": SUB,
        "protagonist": PROTAGONIST,
        "durationSec": "15-20",
        "aspectRatio": "9:16",
        "dialogueLines": 6,
        "subtitle": "EN bold white top / ZH-Hans light-green bottom",
        "audio": "EN only",
        "for": "doubao",
    }
    tags_json = json.dumps(CONFIG_TAGS, ensure_ascii=False)
    ext_json = json.dumps(ext, ensure_ascii=False)

    print(
        f"""USE arelore_creator;

SET @user_id = {USER_ID};

-- demo: one CONFIG tag + one prompt
INSERT INTO `user_ai_prompt_tag` (`user_id`,`tag_type`,`name`,`color`,`sort_order`,`status`) VALUES
(@user_id,'CONFIG','英语','blue',40,'NORMAL')
ON DUPLICATE KEY UPDATE `color`=VALUES(`color`),`sort_order`=VALUES(`sort_order`),`status`=VALUES(`status`),`tag_type`=VALUES(`tag_type`);

INSERT INTO `user_ai_prompt`
(`user_id`,`prompt_uuid`,`title`,`prompt_text`,`config_tags`,`manual_tags`,`publish_status`,`status`,`ext_info`)
VALUES
(@user_id,'{uuid}','{sql_str(TITLE)}','{sql_str(PROMPT)}',
CAST('{sql_str(tags_json)}' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',
CAST('{sql_str(ext_json)}' AS JSON));
"""
    )


if __name__ == "__main__":
    main()
