USE arelore_creator;

-- ============================================================================
-- 种子：英语情景对话（中国）200条 + CONFIG 标签
-- user_id=26882207331096889723
-- title 短引流；summary 独立引流文案含#话题；prompt 按主角生成（9:16，上英下中，仅英音）；同主角形象轮换穿着
-- 稳定 uuid=md5(en-cn-dialogue-china-200|protagonist|category|subscene)
-- 10类×10子场景×2主角；ON DUPLICATE KEY UPDATE
-- ============================================================================

SET @user_id = 26882207331096889723;

-- 仅 UPSERT，不 DELETE（uk: user_id+tag_type+name / user_id+prompt_uuid）
INSERT INTO `user_ai_prompt_tag` (`user_id`,`tag_type`,`name`,`color`,`sort_order`,`status`) VALUES
(@user_id,'CONFIG','抖音','red',10,'NORMAL'),
(@user_id,'CONFIG','视频号','green',20,'NORMAL'),
(@user_id,'CONFIG','小红书','magenta',30,'NORMAL'),
(@user_id,'CONFIG','英语','blue',40,'NORMAL'),
(@user_id,'CONFIG','英语情景对话','geekblue',50,'NORMAL'),
(@user_id,'CONFIG','问路','cyan',60,'NORMAL'),
(@user_id,'CONFIG','点餐','cyan',70,'NORMAL'),
(@user_id,'CONFIG','购物','cyan',80,'NORMAL'),
(@user_id,'CONFIG','交通出行','cyan',90,'NORMAL'),
(@user_id,'CONFIG','酒店住宿','cyan',100,'NORMAL'),
(@user_id,'CONFIG','就医问诊','cyan',110,'NORMAL'),
(@user_id,'CONFIG','校园生活','cyan',120,'NORMAL'),
(@user_id,'CONFIG','银行邮政','cyan',130,'NORMAL'),
(@user_id,'CONFIG','日常服务','cyan',140,'NORMAL'),
(@user_id,'CONFIG','家庭互助','cyan',150,'NORMAL'),
(@user_id,'CONFIG','小女孩','purple',210,'NORMAL'),
(@user_id,'CONFIG','小男孩','purple',220,'NORMAL'),
(@user_id,'CONFIG','少女','purple',230,'NORMAL'),
(@user_id,'CONFIG','少年','purple',240,'NORMAL'),
(@user_id,'CONFIG','年轻女性','purple',250,'NORMAL'),
(@user_id,'CONFIG','年轻男性','purple',260,'NORMAL'),
(@user_id,'CONFIG','中年女性','purple',270,'NORMAL'),
(@user_id,'CONFIG','中年男性','purple',280,'NORMAL'),
(@user_id,'CONFIG','老奶奶','purple',290,'NORMAL'),
(@user_id,'CONFIG','老爷爷','purple',300,'NORMAL')
ON DUPLICATE KEY UPDATE `color`=VALUES(`color`),`sort_order`=VALUES(`sort_order`),`status`=VALUES(`status`),`tag_type`=VALUES(`tag_type`);

INSERT INTO `user_ai_prompt` (`user_id`,`prompt_uuid`,`title`,`prompt_text`,`config_tags`,`manual_tags`,`publish_status`,`status`,`ext_info`) VALUES
(@user_id,'cf931d3c5e2e338bbfa382b6428f692d','小女孩·公交车英语🚌','生成要求：9:16竖屏，15-20秒。中国城市路边公交站台，有中文站牌与候车亭。语速明快。风格：真实生活记录风。

画面描述：小女孩（约6-10岁，马尾辫配粉色发卡，浅色短袖连衣裙，活泼自然）正在「问路/公交车」场景中，在「公交车」用英语完成沟通，主角是小女孩。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

小女孩: I''m looking for the bus stop. Is it far? (我在找the bus stop。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

小女孩: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

小女孩: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

小女孩: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "问路不会开口？站台这一句「Excuse me」先救场。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "公交车", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b81d0cdf11f40fbdc98da17bf6d944f1','公交车英语跟读🚌','生成要求：9:16竖屏，15-20秒。中国城市路边公交站台，有中文站牌与候车亭。语速明快。风格：真实生活记录风。

画面描述：年轻男性（约20-30岁，短发整齐，休闲西装外套，干练友善）正在「问路/公交车」场景中，在「公交车」用英语完成沟通，主角是年轻男性。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

年轻男性: I''m looking for the bus stop. Is it far? (我在找the bus stop。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

年轻男性: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

年轻男性: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

年轻男性: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "问路不会开口？站台这一句「Excuse me」先救场。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "公交车", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'75bfdfd2fe9768c12377c08641a2fd52','英语·地铁现场🚇','生成要求：9:16竖屏，15-20秒。中国地铁站闸机附近，有换乘指示牌。语速明快。风格：真实生活记录风。

画面描述：小男孩（约6-10岁，短发，蓝白条纹T恤与运动鞋，好奇爱提问）正在「问路/地铁」场景中，在「地铁」用英语完成沟通，主角是小男孩。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

小男孩: I''m looking for the subway exit. Is it far? (我在找the subway exit。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

小男孩: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

小男孩: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

小男孩: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "地铁口迷路别慌，跟读这几句就能问出口。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "地铁", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b9505ff6dd8c1972d4a56e26d85997d7','地铁开口说英语🚇','生成要求：9:16竖屏，15-20秒。中国地铁站闸机附近，有换乘指示牌。语速明快。风格：真实生活记录风。

画面描述：中年女性（约35-50岁，齐肩短发，得体风衣与平底鞋，沉稳耐心）正在「问路/地铁」场景中，在「地铁」用英语完成沟通，主角是中年女性。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

中年女性: I''m looking for the subway exit. Is it far? (我在找the subway exit。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

中年女性: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

中年女性: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

中年女性: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "地铁口迷路别慌，跟读这几句就能问出口。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "地铁", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b0ea344a36a3da068b4aab1f8e9b21a3','公园开口说英语🌳','生成要求：9:16竖屏，15-20秒。中国城市公园入口，绿树与中文导览牌。语速明快。风格：人间烟火风。

画面描述：少女（约15-18岁，长直发，校服白衬衫深蓝裙，礼貌大方）正在「问路/公园」场景中，在「公园」用英语完成沟通，主角是少女。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

少女: I''m looking for the park entrance. Is it far? (我在找the park entrance。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

少女: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

少女: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

少女: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "公园问路超生活，一句一句跟着说。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "公园", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3c8ba6a05e4ad81f93dc97ca39cc9d2e','中年男性版公园英语🌳','生成要求：9:16竖屏，15-20秒。中国城市公园入口，绿树与中文导览牌。语速明快。风格：人间烟火风。

画面描述：中年男性（约35-50岁，短发微白，衬衫与夹克，可靠亲切）正在「问路/公园」场景中，在「公园」用英语完成沟通，主角是中年男性。配角为热心路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；热心路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

热心路人: Sure. Where would you like to go? (当然可以。你想去哪里？)

中年男性: I''m looking for the park entrance. Is it far? (我在找the park entrance。远吗？)

热心路人: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

中年男性: Should I turn left or right next? (接下来往左还是往右？)

热心路人: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

中年男性: Is there a quicker way? (有更快捷的路吗？)

热心路人: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

中年男性: Thank you so much. (太谢谢了。)

热心路人: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "公园问路超生活，一句一句跟着说。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "公园", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'45100a9371eb72a4385f7e676518f2fa','跟读|景区英语🧭','生成要求：9:16竖屏，15-20秒。中国景区售票处外，人流与导览图。语速明快。风格：旅行记录风。

画面描述：少年（约15-18岁，短发清爽，运动服套装，礼貌稳重）正在「问路/景区」场景中，在「景区」用英语完成沟通，主角是少年。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

少年: I''m looking for the ticket office. Is it far? (我在找the ticket office。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

少年: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

少年: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

少年: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "景区门口被围住？这几句指路英语够用。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "景区", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'41e7ea4108951a943c71f1afc974f56d','景区情景英语🧭','生成要求：9:16竖屏，15-20秒。中国景区售票处外，人流与导览图。语速明快。风格：旅行记录风。

画面描述：老奶奶（约60+岁，花白短发，碎花布衣与薄外套，笑容温和）正在「问路/景区」场景中，在「景区」用英语完成沟通，主角是老奶奶。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

老奶奶: I''m looking for the ticket office. Is it far? (我在找the ticket office。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

老奶奶: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

老奶奶: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

老奶奶: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "景区门口被围住？这几句指路英语够用。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "景区", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'224663358409044ddec1cc1096dfd8aa','生活英语·商场🚌','生成要求：9:16竖屏，15-20秒。中国大型商场中庭，有楼层指示。语速明快。风格：都市生活风。

画面描述：年轻女性（约20-30岁，披肩长发，米色风衣与乐福鞋，自信自然）正在「问路/商场」场景中，在「商场」用英语完成沟通，主角是年轻女性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

服务员: Sure. Where would you like to go? (当然可以。你想去哪里？)

年轻女性: I''m looking for the shopping mall. Is it far? (我在找the shopping mall。远吗？)

服务员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

年轻女性: Should I turn left or right next? (接下来往左还是往右？)

服务员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

年轻女性: Is there a quicker way? (有更快捷的路吗？)

服务员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

年轻女性: Thank you so much. (太谢谢了。)

服务员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "商场找店别比划，用地道英语问楼层。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "商场", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'bce134bc40d6f9b20e7467c421185df9','老爷爷的商场英语🚌','生成要求：9:16竖屏，15-20秒。中国大型商场中庭，有楼层指示。语速明快。风格：都市生活风。

画面描述：老爷爷（约60+岁，花白短发，衬衫与马甲，慈祥耐心）正在「问路/商场」场景中，在「商场」用英语完成沟通，主角是老爷爷。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

服务员: Sure. Where would you like to go? (当然可以。你想去哪里？)

老爷爷: I''m looking for the shopping mall. Is it far? (我在找the shopping mall。远吗？)

服务员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

老爷爷: Should I turn left or right next? (接下来往左还是往右？)

服务员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

老爷爷: Is there a quicker way? (有更快捷的路吗？)

服务员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

老爷爷: Thank you so much. (太谢谢了。)

服务员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "商场找店别比划，用地道英语问楼层。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "商场", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3c5cf1ccbdd9e4ec7a61d70afa265765','年轻男性·外地学校英语🚌','生成要求：9:16竖屏，15-20秒。中国大学校园门口，中文校名牌。语速明快。风格：青春校园风。

画面描述：年轻男性（约22-28岁，微长碎发，连帽卫衣与牛仔裤，轻松）正在「问路/外地学校」场景中，在「外地学校」用英语完成沟通，主角是年轻男性。配角为学生。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；学生声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

学生: Sure. Where would you like to go? (当然可以。你想去哪里？)

年轻男性: I''m looking for the school gate. Is it far? (我在找the school gate。远吗？)

学生: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

年轻男性: Should I turn left or right next? (接下来往左还是往右？)

学生: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

年轻男性: Is there a quicker way? (有更快捷的路吗？)

学生: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

年轻男性: Thank you so much. (太谢谢了。)

学生: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外地校园问路，礼貌又清楚。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "外地学校", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'55da9e29f22a6b73bff34ab865a03ea3','外地学校英语跟读🚌','生成要求：9:16竖屏，15-20秒。中国大学校园门口，中文校名牌。语速明快。风格：青春校园风。

画面描述：小女孩（约6-10岁，戴遮阳帽与小书包，格子短袖短裤，好奇张望）正在「问路/外地学校」场景中，在「外地学校」用英语完成沟通，主角是小女孩。配角为学生。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；学生声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

学生: Sure. Where would you like to go? (当然可以。你想去哪里？)

小女孩: I''m looking for the school gate. Is it far? (我在找the school gate。远吗？)

学生: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

小女孩: Should I turn left or right next? (接下来往左还是往右？)

学生: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

小女孩: Is there a quicker way? (有更快捷的路吗？)

学生: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

小女孩: Thank you so much. (太谢谢了。)

学生: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外地校园问路，礼貌又清楚。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "外地学校", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8649d0e57e1632c39daa38f8662f5fc4','英语·医院现场🚌','生成要求：9:16竖屏，15-20秒。中国医院门诊大厅导诊台附近。语速明快。风格：实用生活风。

画面描述：中年女性（约38-48岁，微卷中长发，针织衫配长裙，温和）正在「问路/医院」场景中，在「医院」用英语完成沟通，主角是中年女性。配角为导诊员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；导诊员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

导诊员: Sure. Where would you like to go? (当然可以。你想去哪里？)

中年女性: I''m looking for the hospital lobby. Is it far? (我在找the hospital lobby。远吗？)

导诊员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

中年女性: Should I turn left or right next? (接下来往左还是往右？)

导诊员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

中年女性: Is there a quicker way? (有更快捷的路吗？)

导诊员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

中年女性: Thank you so much. (太谢谢了。)

导诊员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "医院大厅第一次问诊路线，英语也能说清。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "医院", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b805dd37d9be8adbf60985d43ab0f165','医院开口说英语🚌','生成要求：9:16竖屏，15-20秒。中国医院门诊大厅导诊台附近。语速明快。风格：实用生活风。

画面描述：小男孩（约7-9岁，戴鸭舌帽，迷彩短裤与背心，活力十足）正在「问路/医院」场景中，在「医院」用英语完成沟通，主角是小男孩。配角为导诊员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；导诊员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

导诊员: Sure. Where would you like to go? (当然可以。你想去哪里？)

小男孩: I''m looking for the hospital lobby. Is it far? (我在找the hospital lobby。远吗？)

导诊员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

小男孩: Should I turn left or right next? (接下来往左还是往右？)

导诊员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

小男孩: Is there a quicker way? (有更快捷的路吗？)

导诊员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

小男孩: Thank you so much. (太谢谢了。)

导诊员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "医院大厅第一次问诊路线，英语也能说清。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "医院", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c9ce2f0b94c86a7650dda95c56786333','火车站开口说英语🚄','生成要求：9:16竖屏，15-20秒。中国火车站进站口，有车次大屏。语速明快。风格：出行实用风。

画面描述：中年男性（约38-48岁，寸头，Polo衫与卡其裤，朴实）正在「问路/火车站」场景中，在「火车站」用英语完成沟通，主角是中年男性。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

中年男性: I''m looking for the train station. Is it far? (我在找the train station。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

中年男性: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

中年男性: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

中年男性: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火车站进站口高频句，听一遍就会问。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "火车站", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b193ae14a55b36948f634016bf55a2de','少女版火车站英语🚄','生成要求：9:16竖屏，15-20秒。中国火车站进站口，有车次大屏。语速明快。风格：出行实用风。

画面描述：少女（约16-18岁，低马尾，浅色针织开衫与牛仔裤，安静文雅）正在「问路/火车站」场景中，在「火车站」用英语完成沟通，主角是少女。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

少女: I''m looking for the train station. Is it far? (我在找the train station。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

少女: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

少女: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

少女: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火车站进站口高频句，听一遍就会问。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "火车站", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'173b953ef1fe8e16dc439ae485d77541','跟读|机场英语✈️','生成要求：9:16竖屏，15-20秒。中国机场到达大厅问询处。语速明快。风格：出行实用风。

画面描述：老奶奶（约62-75岁，盘发插簪，深色棉袄与布鞋，慈祥）正在「问路/机场」场景中，在「机场」用英语完成沟通，主角是老奶奶。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

老奶奶: I''m looking for the airport information desk. Is it far? (我在找the airport information desk。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

老奶奶: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

老奶奶: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

老奶奶: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "机场到达大厅问询，别再只会点头。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "机场", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b40e73965c6e503c9d22436515b8159c','机场情景英语✈️','生成要求：9:16竖屏，15-20秒。中国机场到达大厅问询处。语速明快。风格：出行实用风。

画面描述：少年（约16-18岁，微乱短发，连帽卫衣与牛仔裤，随性）正在「问路/机场」场景中，在「机场」用英语完成沟通，主角是少年。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

工作人员: Sure. Where would you like to go? (当然可以。你想去哪里？)

少年: I''m looking for the airport information desk. Is it far? (我在找the airport information desk。远吗？)

工作人员: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

少年: Should I turn left or right next? (接下来往左还是往右？)

工作人员: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

少年: Is there a quicker way? (有更快捷的路吗？)

工作人员: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

少年: Thank you so much. (太谢谢了。)

工作人员: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "机场到达大厅问询，别再只会点头。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "机场", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4efbf786eae8fb03e5a7f3038f459425','生活英语·居民小区🚌','生成要求：9:16竖屏，15-20秒。中国居民小区门口，有门禁与快递柜。语速明快。风格：邻里生活风。

画面描述：老爷爷（约62-75岁，白发寸头，中山装式外套，精神矍铄）正在「问路/居民小区」场景中，在「居民小区」用英语完成沟通，主角是老爷爷。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

保安: Sure. Where would you like to go? (当然可以。你想去哪里？)

老爷爷: I''m looking for the community gate. Is it far? (我在找the community gate。远吗？)

保安: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

老爷爷: Should I turn left or right next? (接下来往左还是往右？)

保安: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

老爷爷: Is there a quicker way? (有更快捷的路吗？)

保安: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

老爷爷: Thank you so much. (太谢谢了。)

保安: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "小区门口问路，保安都能接住你的英语。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "居民小区", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6333dfe088d898bef3d562896c305588','年轻女性的居民小区英语🚌','生成要求：9:16竖屏，15-20秒。中国居民小区门口，有门禁与快递柜。语速明快。风格：邻里生活风。

画面描述：年轻女性（约22-28岁，低盘发，白衬衫配西裤，干练通勤）正在「问路/居民小区」场景中，在「居民小区」用英语完成沟通，主角是年轻女性。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, could you help me find my way? (不好意思，能帮我指一下路吗？)

保安: Sure. Where would you like to go? (当然可以。你想去哪里？)

年轻女性: I''m looking for the community gate. Is it far? (我在找the community gate。远吗？)

保安: About ten minutes on foot if you go straight. (直走大概步行十分钟。)

年轻女性: Should I turn left or right next? (接下来往左还是往右？)

保安: Turn right, then follow the signs. (往右拐，然后跟着指示牌。)

年轻女性: Is there a quicker way? (有更快捷的路吗？)

保安: Yes. Take that entrance and keep going. (有，从那个入口进，一直走。)

年轻女性: Thank you so much. (太谢谢了。)

保安: You''re welcome. Have a nice day in China! (不客气。祝你在中国愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "问路", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "小区门口问路，保安都能接住你的英语。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "问路", "sceneSub": "居民小区", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'54ba151b5eea079b6b37875f400b18ea','少年·中餐馆英语🍜','生成要求：9:16竖屏，15-20秒。中国中式餐馆桌边，菜单与餐具整齐。语速明快。风格：美食记录风。

画面描述：少年（约15-17岁，侧分头，白衬衫校服裤，书包双肩）正在「点餐/中餐馆」场景中，在「中餐馆」用英语完成沟通，主角是少年。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

少年: I''d like a bowl of tomato egg noodles. Is it spicy? (我想要a bowl of tomato egg noodles。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

少年: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

少年: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

少年: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "中餐馆点番茄蛋面，微辣不要香菜怎么说。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "中餐馆", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'cba01a2fc1471969d14402520885c588','中餐馆英语跟读🍜','生成要求：9:16竖屏，15-20秒。中国中式餐馆桌边，菜单与餐具整齐。语速明快。风格：美食记录风。

画面描述：老奶奶（约60-70岁，齐耳白发，针织开衫与长裤，清爽）正在「点餐/中餐馆」场景中，在「中餐馆」用英语完成沟通，主角是老奶奶。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

老奶奶: I''d like a bowl of tomato egg noodles. Is it spicy? (我想要a bowl of tomato egg noodles。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

老奶奶: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

老奶奶: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

老奶奶: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "中餐馆点番茄蛋面，微辣不要香菜怎么说。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "中餐馆", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d0be4023d635aeb5e58cd6e35d4d6f8e','英语·火锅店现场🍲','生成要求：9:16竖屏，15-20秒。中国火锅店桌边，锅底冒着热气。语速明快。风格：热闹市井风。

画面描述：年轻女性（约20-26岁，齐肩波浪发，针织衫与牛仔裙，柔和）正在「点餐/火锅店」场景中，在「火锅店」用英语完成沟通，主角是年轻女性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

年轻女性: I''d like a mild hotpot set. Is it spicy? (我想要a mild hotpot set。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

年轻女性: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

年轻女性: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

年轻女性: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火锅店开口点餐，别只会竖大拇指。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "火锅店", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b062c82c4e92a5df69d6a10502c9b369','火锅店开口说英语🍲','生成要求：9:16竖屏，15-20秒。中国火锅店桌边，锅底冒着热气。语速明快。风格：热闹市井风。

画面描述：老爷爷（约60-70岁，银框眼镜，羊毛衫与长裤，温和）正在「点餐/火锅店」场景中，在「火锅店」用英语完成沟通，主角是老爷爷。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

老爷爷: I''d like a mild hotpot set. Is it spicy? (我想要a mild hotpot set。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

老爷爷: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

老爷爷: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

老爷爷: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火锅店开口点餐，别只会竖大拇指。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "火锅店", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a80818401cb870f34d54be96b1521e30','奶茶店开口说英语🧋','生成要求：9:16竖屏，15-20秒。中国奶茶店柜台前，点单屏幕清晰。语速明快。风格：轻快生活风。

画面描述：年轻男性（约24-30岁，侧分头，白衬衫卷袖与斜挎包，利落）正在「点餐/奶茶店」场景中，在「奶茶店」用英语完成沟通，主角是年轻男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

店员: Of course. Here you are. (当然，给您。)

年轻男性: I''d like a less-sugar milk tea. Is it spicy? (我想要a less-sugar milk tea。辣吗？)

店员: A little. We can make it mild. (有一点，可以做成微辣。)

年轻男性: Could I get it without cilantro? (能不要香菜吗？)

店员: No problem. Anything to drink? (没问题。喝点什么？)

年轻男性: Just warm water, please. (只要温水就好。)

店员: Okay. It will be ready soon. (好的，马上就好。)

年轻男性: How much is it in total? (一共多少钱？)

店员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "奶茶店柜台点单，甜度冰度一次说清。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "奶茶店", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'93630e36765c8eaf9d8acb7c9f5c5764','小女孩版奶茶店英语🧋','生成要求：9:16竖屏，15-20秒。中国奶茶店柜台前，点单屏幕清晰。语速明快。风格：轻快生活风。

画面描述：小女孩（约7-9岁，齐肩发，条纹T恤配背带裤，笑容明亮）正在「点餐/奶茶店」场景中，在「奶茶店」用英语完成沟通，主角是小女孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

店员: Of course. Here you are. (当然，给您。)

小女孩: I''d like a less-sugar milk tea. Is it spicy? (我想要a less-sugar milk tea。辣吗？)

店员: A little. We can make it mild. (有一点，可以做成微辣。)

小女孩: Could I get it without cilantro? (能不要香菜吗？)

店员: No problem. Anything to drink? (没问题。喝点什么？)

小女孩: Just warm water, please. (只要温水就好。)

店员: Okay. It will be ready soon. (好的，马上就好。)

小女孩: How much is it in total? (一共多少钱？)

店员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "奶茶店柜台点单，甜度冰度一次说清。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "奶茶店", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4ce0c3cb4e53aee00161e8b399b004c1','跟读|咖啡店英语☕','生成要求：9:16竖屏，15-20秒。中国街边咖啡店，木质吧台。语速明快。风格：都市轻生活风。

画面描述：中年女性（约35-45岁，利落短发，衬衫与西裤套装，干练）正在「点餐/咖啡店」场景中，在「咖啡店」用英语完成沟通，主角是中年女性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

店员: Of course. Here you are. (当然，给您。)

中年女性: I''d like an iced Americano. Is it spicy? (我想要an iced Americano。辣吗？)

店员: A little. We can make it mild. (有一点，可以做成微辣。)

中年女性: Could I get it without cilantro? (能不要香菜吗？)

店员: No problem. Anything to drink? (没问题。喝点什么？)

中年女性: Just warm water, please. (只要温水就好。)

店员: Okay. It will be ready soon. (好的，马上就好。)

中年女性: How much is it in total? (一共多少钱？)

店员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "咖啡店点单不慌，这几句超日常。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "咖啡店", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a2f777067918bdbde6ba59e89a9f5004','咖啡店情景英语☕','生成要求：9:16竖屏，15-20秒。中国街边咖啡店，木质吧台。语速明快。风格：都市轻生活风。

画面描述：小男孩（约6-8岁，圆寸头，校服式白衬衫卷袖，书包斜挎）正在「点餐/咖啡店」场景中，在「咖啡店」用英语完成沟通，主角是小男孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

店员: Of course. Here you are. (当然，给您。)

小男孩: I''d like an iced Americano. Is it spicy? (我想要an iced Americano。辣吗？)

店员: A little. We can make it mild. (有一点，可以做成微辣。)

小男孩: Could I get it without cilantro? (能不要香菜吗？)

店员: No problem. Anything to drink? (没问题。喝点什么？)

小男孩: Just warm water, please. (只要温水就好。)

店员: Okay. It will be ready soon. (好的，马上就好。)

小男孩: How much is it in total? (一共多少钱？)

店员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "咖啡店点单不慌，这几句超日常。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "咖啡店", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ac77248b0c9c7e3bc211b2ca2c84b328','生活英语·单位食堂🍜','生成要求：9:16竖屏，15-20秒。中国单位食堂窗口，有菜名牌子。语速明快。风格：日常实用风。

画面描述：中年男性（约40-50岁，黑框眼镜，深色西装外套便装化，稳重）正在「点餐/单位食堂」场景中，在「单位食堂」用英语完成沟通，主角是中年男性。配角为打菜阿姨。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；打菜阿姨声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

打菜阿姨: Of course. Here you are. (当然，给您。)

中年男性: I''d like the rice with vegetables. Is it spicy? (我想要the rice with vegetables。辣吗？)

打菜阿姨: A little. We can make it mild. (有一点，可以做成微辣。)

中年男性: Could I get it without cilantro? (能不要香菜吗？)

打菜阿姨: No problem. Anything to drink? (没问题。喝点什么？)

中年男性: Just warm water, please. (只要温水就好。)

打菜阿姨: Okay. It will be ready soon. (好的，马上就好。)

中年男性: How much is it in total? (一共多少钱？)

打菜阿姨: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "食堂窗口打菜也能练口语。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "单位食堂", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6fd77974d4d32330d10c8b2bdb1ce06c','少女的单位食堂英语🍜','生成要求：9:16竖屏，15-20秒。中国单位食堂窗口，有菜名牌子。语速明快。风格：日常实用风。

画面描述：少女（约15-17岁，齐肩黑发，卫衣配帆布鞋，校园休闲感）正在「点餐/单位食堂」场景中，在「单位食堂」用英语完成沟通，主角是少女。配角为打菜阿姨。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；打菜阿姨声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

打菜阿姨: Of course. Here you are. (当然，给您。)

少女: I''d like the rice with vegetables. Is it spicy? (我想要the rice with vegetables。辣吗？)

打菜阿姨: A little. We can make it mild. (有一点，可以做成微辣。)

少女: Could I get it without cilantro? (能不要香菜吗？)

打菜阿姨: No problem. Anything to drink? (没问题。喝点什么？)

少女: Just warm water, please. (只要温水就好。)

打菜阿姨: Okay. It will be ready soon. (好的，马上就好。)

少女: How much is it in total? (一共多少钱？)

打菜阿姨: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "食堂窗口打菜也能练口语。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "单位食堂", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'85272ffa567138a1ebe463568ccdea2a','老奶奶·街头小吃摊英语🍜','生成要求：9:16竖屏，15-20秒。中国街头小吃摊，蒸汽与香气。语速明快。风格：烟火气风。

画面描述：老奶奶（约65-78岁，头巾轻裹，格子外套与布包，家常）正在「点餐/街头小吃摊」场景中，在「街头小吃摊」用英语完成沟通，主角是老奶奶。配角为摊主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；摊主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

摊主: Of course. Here you are. (当然，给您。)

老奶奶: I''d like a jianbing. Is it spicy? (我想要a jianbing。辣吗？)

摊主: A little. We can make it mild. (有一点，可以做成微辣。)

老奶奶: Could I get it without cilantro? (能不要香菜吗？)

摊主: No problem. Anything to drink? (没问题。喝点什么？)

老奶奶: Just warm water, please. (只要温水就好。)

摊主: Okay. It will be ready soon. (好的，马上就好。)

老奶奶: How much is it in total? (一共多少钱？)

摊主: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "街头小吃摊点餐，烟火气英语来了。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "街头小吃摊", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'cf5419005847a70bad9bf4f1b0cf4fa6','街头小吃摊英语跟读🍜','生成要求：9:16竖屏，15-20秒。中国街头小吃摊，蒸汽与香气。语速明快。风格：烟火气风。

画面描述：少年（约16-18岁，寸头，深色夹克配帆布鞋，干练）正在「点餐/街头小吃摊」场景中，在「街头小吃摊」用英语完成沟通，主角是少年。配角为摊主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；摊主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

摊主: Of course. Here you are. (当然，给您。)

少年: I''d like a jianbing. Is it spicy? (我想要a jianbing。辣吗？)

摊主: A little. We can make it mild. (有一点，可以做成微辣。)

少年: Could I get it without cilantro? (能不要香菜吗？)

摊主: No problem. Anything to drink? (没问题。喝点什么？)

少年: Just warm water, please. (只要温水就好。)

摊主: Okay. It will be ready soon. (好的，马上就好。)

少年: How much is it in total? (一共多少钱？)

摊主: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "街头小吃摊点餐，烟火气英语来了。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "街头小吃摊", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8358e9fa4eec05f04de1433e7712f09d','英语·西餐厅现场🍜','生成要求：9:16竖屏，15-20秒。中国西餐厅卡座，灯光柔和。语速明快。风格：精致生活风。

画面描述：老爷爷（约65-78岁，花白寸发，深蓝夹克与布帽，朴实）正在「点餐/西餐厅」场景中，在「西餐厅」用英语完成沟通，主角是老爷爷。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

老爷爷: I''d like the pasta of the day. Is it spicy? (我想要the pasta of the day。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

老爷爷: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

老爷爷: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

老爷爷: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "西餐厅卡座点餐，礼貌又自然。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "西餐厅", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'968ce9d54936d13501d15f99bdfaf558','西餐厅开口说英语🍜','生成要求：9:16竖屏，15-20秒。中国西餐厅卡座，灯光柔和。语速明快。风格：精致生活风。

画面描述：年轻女性（约24-30岁，高马尾，运动夹克与瑜伽裤，轻快）正在「点餐/西餐厅」场景中，在「西餐厅」用英语完成沟通，主角是年轻女性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

年轻女性: I''d like the pasta of the day. Is it spicy? (我想要the pasta of the day。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

年轻女性: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

年轻女性: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

年轻女性: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "西餐厅卡座点餐，礼貌又自然。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "西餐厅", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8a9326e67df2b1d4e4737ac82f634387','自助餐厅开口说英语🍜','生成要求：9:16竖屏，15-20秒。中国自助餐厅取餐区，餐盘与标识。语速明快。风格：轻松实用风。

画面描述：小女孩（约6-8岁，双麻花辫，浅色防晒衣与运动鞋，蹦跳感）正在「点餐/自助餐厅」场景中，在「自助餐厅」用英语完成沟通，主角是小女孩。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

小女孩: I''d like the seafood section. Is it spicy? (我想要the seafood section。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

小女孩: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

小女孩: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

小女孩: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "自助餐厅取餐沟通，实用到离谱。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "自助餐厅", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'2d4bd6bf5a3a299a3c97476b06a426a8','年轻男性版自助餐厅英语🍜','生成要求：9:16竖屏，15-20秒。中国自助餐厅取餐区，餐盘与标识。语速明快。风格：轻松实用风。

画面描述：年轻男性（约20-26岁，寸头，运动外套与跑鞋，精神）正在「点餐/自助餐厅」场景中，在「自助餐厅」用英语完成沟通，主角是年轻男性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

年轻男性: I''d like the seafood section. Is it spicy? (我想要the seafood section。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

年轻男性: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

年轻男性: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

年轻男性: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "自助餐厅取餐沟通，实用到离谱。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "自助餐厅", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ea0a5632b279bc708e4ab2879b7d1862','跟读|外卖取餐柜英语🍜','生成要求：9:16竖屏，15-20秒。中国小区外卖取餐柜前。语速明快。风格：现代生活风。

画面描述：小男孩（约8-10岁，刺猬头，红色运动套装，额头微汗）正在「点餐/外卖取餐柜」场景中，在「外卖取餐柜」用英语完成沟通，主角是小男孩。配角为外卖小哥。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；外卖小哥声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

外卖小哥: Of course. Here you are. (当然，给您。)

小男孩: I''d like my takeout order. Is it spicy? (我想要my takeout order。辣吗？)

外卖小哥: A little. We can make it mild. (有一点，可以做成微辣。)

小男孩: Could I get it without cilantro? (能不要香菜吗？)

外卖小哥: No problem. Anything to drink? (没问题。喝点什么？)

小男孩: Just warm water, please. (只要温水就好。)

外卖小哥: Okay. It will be ready soon. (好的，马上就好。)

小男孩: How much is it in total? (一共多少钱？)

外卖小哥: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外卖柜取餐对话，短平快能跟读。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "外卖取餐柜", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1ef04815d4f149ad18c6dcd87eb9ab12','外卖取餐柜情景英语🍜','生成要求：9:16竖屏，15-20秒。中国小区外卖取餐柜前。语速明快。风格：现代生活风。

画面描述：中年女性（约40-50岁，低盘发，丝巾与大衣，端庄）正在「点餐/外卖取餐柜」场景中，在「外卖取餐柜」用英语完成沟通，主角是中年女性。配角为外卖小哥。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；外卖小哥声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

外卖小哥: Of course. Here you are. (当然，给您。)

中年女性: I''d like my takeout order. Is it spicy? (我想要my takeout order。辣吗？)

外卖小哥: A little. We can make it mild. (有一点，可以做成微辣。)

中年女性: Could I get it without cilantro? (能不要香菜吗？)

外卖小哥: No problem. Anything to drink? (没问题。喝点什么？)

中年女性: Just warm water, please. (只要温水就好。)

外卖小哥: Okay. It will be ready soon. (好的，马上就好。)

中年女性: How much is it in total? (一共多少钱？)

外卖小哥: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外卖柜取餐对话，短平快能跟读。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "外卖取餐柜", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b37823c44b0628388e0a1acafffbef31','生活英语·酒店餐厅🍜','生成要求：9:16竖屏，15-20秒。中国酒店自助餐厅入口。语速明快。风格：旅行生活风。

画面描述：少女（约16-18岁，半扎发，浅色连衣裙与小挎包，清爽）正在「点餐/酒店餐厅」场景中，在「酒店餐厅」用英语完成沟通，主角是少女。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

少女: I''d like the breakfast buffet. Is it spicy? (我想要the breakfast buffet。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

少女: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

少女: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

少女: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店餐厅点餐，出门也能用。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "酒店餐厅", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'82e68c41a3faf854fc24193c696ac8a8','中年男性的酒店餐厅英语🍜','生成要求：9:16竖屏，15-20秒。中国酒店自助餐厅入口。语速明快。风格：旅行生活风。

画面描述：中年男性（约35-45岁，中分短发，格子衬衫与帆布鞋，随和）正在「点餐/酒店餐厅」场景中，在「酒店餐厅」用英语完成沟通，主角是中年男性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, could I see the menu, please? (你好，请给我看看菜单好吗？)

服务员: Of course. Here you are. (当然，给您。)

中年男性: I''d like the breakfast buffet. Is it spicy? (我想要the breakfast buffet。辣吗？)

服务员: A little. We can make it mild. (有一点，可以做成微辣。)

中年男性: Could I get it without cilantro? (能不要香菜吗？)

服务员: No problem. Anything to drink? (没问题。喝点什么？)

中年男性: Just warm water, please. (只要温水就好。)

服务员: Okay. It will be ready soon. (好的，马上就好。)

中年男性: How much is it in total? (一共多少钱？)

服务员: Thirty-eight yuan. Mobile pay is fine. (三十八元，手机支付可以。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "点餐", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店餐厅点餐，出门也能用。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "点餐", "sceneSub": "酒店餐厅", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'77cf43eae9f9c344d2907cd70484e9e6','中年女性·超市英语🛒','生成要求：9:16竖屏，15-20秒。中国超市生鲜区，价签清晰。语速明快。风格：生活实用风。

画面描述：中年女性（约36-46岁，黑框眼镜，棉质外套与帆布包，亲切）正在「购物/超市」场景中，在「超市」用英语完成沟通，主角是中年女性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, where can I find fresh tomatoes? (请问fresh tomatoes在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

中年女性: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

中年女性: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

中年女性: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

中年女性: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "超市生鲜区问价、找货、手机付，一句不少。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "超市", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c706136cada0b12264723c283898744e','超市英语跟读🛒','生成要求：9:16竖屏，15-20秒。中国超市生鲜区，价签清晰。语速明快。风格：生活实用风。

画面描述：小男孩（约6-9岁，侧分短发，格子衬衫配牛仔裤，乖巧）正在「购物/超市」场景中，在「超市」用英语完成沟通，主角是小男孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, where can I find fresh tomatoes? (请问fresh tomatoes在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

小男孩: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

小男孩: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

小男孩: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

小男孩: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "超市生鲜区问价、找货、手机付，一句不少。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "超市", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8f2ac34261992825e954c364ed9cb63c','英语·便利店现场🛒','生成要求：9:16竖屏，15-20秒。中国24小时便利店货架前。语速明快。风格：都市夜生活风。

画面描述：中年男性（约38-50岁，微秃短发，冲锋衣与背包，务实）正在「购物/便利店」场景中，在「便利店」用英语完成沟通，主角是中年男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, where can I find bottled water? (请问bottled water在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

中年男性: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

中年男性: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

中年男性: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

中年男性: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "便利店货架前高频口语，马上能用。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "便利店", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b8671221761fd0c6fda312564b3f9e49','便利店开口说英语🛒','生成要求：9:16竖屏，15-20秒。中国24小时便利店货架前。语速明快。风格：都市夜生活风。

画面描述：少女（约15-18岁，空气刘海，格子衬衫叠穿T恤，元气）正在「购物/便利店」场景中，在「便利店」用英语完成沟通，主角是少女。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, where can I find bottled water? (请问bottled water在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

少女: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

少女: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

少女: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

少女: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "便利店货架前高频口语，马上能用。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "便利店", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'05652e7d96e0b0e6da499b1faa615243','服装店开口说英语🛒','生成要求：9:16竖屏，15-20秒。中国服装店试衣区外。语速明快。风格：时尚生活风。

画面描述：老奶奶（约60-72岁，银发微卷，浅色羽绒服与围巾，温暖）正在「购物/服装店」场景中，在「服装店」用英语完成沟通，主角是老奶奶。配角为导购。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；导购声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, where can I find this jacket? (请问this jacket在哪里？)

导购: On that shelf to your right. (在您右边那个架子上。)

老奶奶: Do you have a smaller size? (有小一点的吗？)

导购: Yes, let me check the stock. (有，我帮您看看库存。)

老奶奶: How much is this? (这个多少钱？)

导购: Twenty-five yuan. (二十五元。)

老奶奶: Can I pay by phone? (可以手机支付吗？)

导购: Sure. Scan this code. (可以，扫这个码。)

老奶奶: Could I have a bag, please? (能给我一个袋子吗？)

导购: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "服装店试衣问尺码，导购对话超真实。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "服装店", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4227e36d16e306deaf4a3dcc6531ff14','少年版服装店英语🛒','生成要求：9:16竖屏，15-20秒。中国服装店试衣区外。语速明快。风格：时尚生活风。

画面描述：少年（约15-18岁，棒球服外套，耳机挂颈，轻松）正在「购物/服装店」场景中，在「服装店」用英语完成沟通，主角是少年。配角为导购。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；导购声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, where can I find this jacket? (请问this jacket在哪里？)

导购: On that shelf to your right. (在您右边那个架子上。)

少年: Do you have a smaller size? (有小一点的吗？)

导购: Yes, let me check the stock. (有，我帮您看看库存。)

少年: How much is this? (这个多少钱？)

导购: Twenty-five yuan. (二十五元。)

少年: Can I pay by phone? (可以手机支付吗？)

导购: Sure. Scan this code. (可以，扫这个码。)

少年: Could I have a bag, please? (能给我一个袋子吗？)

导购: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "服装店试衣问尺码，导购对话超真实。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "服装店", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'0b7332f8b8457038df1072147eca5026','跟读|药店英语🛒','生成要求：9:16竖屏，15-20秒。中国药店柜台，药柜整齐。语速明快。风格：实用生活风。

画面描述：老爷爷（约63-75岁，白眉短发，唐装上衣与布鞋，从容）正在「购物/药店」场景中，在「药店」用英语完成沟通，主角是老爷爷。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, where can I find cold medicine? (请问cold medicine在哪里？)

药师: On that shelf to your right. (在您右边那个架子上。)

老爷爷: Do you have a smaller size? (有小一点的吗？)

药师: Yes, let me check the stock. (有，我帮您看看库存。)

老爷爷: How much is this? (这个多少钱？)

药师: Twenty-five yuan. (二十五元。)

老爷爷: Can I pay by phone? (可以手机支付吗？)

药师: Sure. Scan this code. (可以，扫这个码。)

老爷爷: Could I have a bag, please? (能给我一个袋子吗？)

药师: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "药店柜台问药，说清楚症状很关键。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "药店", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'eb71887060337c85729583715de23ca1','药店情景英语🛒','生成要求：9:16竖屏，15-20秒。中国药店柜台，药柜整齐。语速明快。风格：实用生活风。

画面描述：年轻女性（约21-27岁，短发利落，条纹衬衫与托特包，清爽）正在「购物/药店」场景中，在「药店」用英语完成沟通，主角是年轻女性。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, where can I find cold medicine? (请问cold medicine在哪里？)

药师: On that shelf to your right. (在您右边那个架子上。)

年轻女性: Do you have a smaller size? (有小一点的吗？)

药师: Yes, let me check the stock. (有，我帮您看看库存。)

年轻女性: How much is this? (这个多少钱？)

药师: Twenty-five yuan. (二十五元。)

年轻女性: Can I pay by phone? (可以手机支付吗？)

药师: Sure. Scan this code. (可以，扫这个码。)

年轻女性: Could I have a bag, please? (能给我一个袋子吗？)

药师: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "药店柜台问药，说清楚症状很关键。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "药店", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d4ae19a3e36957099c342b09311a1593','生活英语·书店🛒','生成要求：9:16竖屏，15-20秒。中国书店文学区，书架与阅读角。语速明快。风格：文艺治愈风。

画面描述：小女孩（约8-10岁，戴棒球帽，卡通印花短袖，背迷你双肩包）正在「购物/书店」场景中，在「书店」用英语完成沟通，主角是小女孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, where can I find an English storybook? (请问an English storybook在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

小女孩: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

小女孩: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

小女孩: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

小女孩: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "书店找书借阅感口语，安静也要会说。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "书店", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3dc34d20c2415b9595205122ea85dddb','年轻男性的书店英语🛒','生成要求：9:16竖屏，15-20秒。中国书店文学区，书架与阅读角。语速明快。风格：文艺治愈风。

画面描述：年轻男性（约23-29岁，黑框眼镜，针织开衫与卡其裤，温和）正在「购物/书店」场景中，在「书店」用英语完成沟通，主角是年轻男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, where can I find an English storybook? (请问an English storybook在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

年轻男性: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

年轻男性: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

年轻男性: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

年轻男性: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "书店找书借阅感口语，安静也要会说。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "书店", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8a08fc48e0477fc5636c1471c3cce4b5','小男孩·电器城英语🛒','生成要求：9:16竖屏，15-20秒。中国电器卖场展台前。语速明快。风格：消费实用风。

画面描述：小男孩（约7-10岁，棒球帽反戴，印花短袖，滑板鞋）正在「购物/电器城」场景中，在「电器城」用英语完成沟通，主角是小男孩。配角为导购。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；导购声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, where can I find a power bank? (请问a power bank在哪里？)

导购: On that shelf to your right. (在您右边那个架子上。)

小男孩: Do you have a smaller size? (有小一点的吗？)

导购: Yes, let me check the stock. (有，我帮您看看库存。)

小男孩: How much is this? (这个多少钱？)

导购: Twenty-five yuan. (二十五元。)

小男孩: Can I pay by phone? (可以手机支付吗？)

导购: Sure. Scan this code. (可以，扫这个码。)

小男孩: Could I have a bag, please? (能给我一个袋子吗？)

导购: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "电器城问参数别慌，跟导购对上几句。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "电器城", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'aebfa827ab21af30444f2c58d7783057','电器城英语跟读🛒','生成要求：9:16竖屏，15-20秒。中国电器卖场展台前。语速明快。风格：消费实用风。

画面描述：中年女性（约38-50岁，染浅棕发，条纹衬衫卷袖，日常家居外出发型）正在「购物/电器城」场景中，在「电器城」用英语完成沟通，主角是中年女性。配角为导购。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；导购声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, where can I find a power bank? (请问a power bank在哪里？)

导购: On that shelf to your right. (在您右边那个架子上。)

中年女性: Do you have a smaller size? (有小一点的吗？)

导购: Yes, let me check the stock. (有，我帮您看看库存。)

中年女性: How much is this? (这个多少钱？)

导购: Twenty-five yuan. (二十五元。)

中年女性: Can I pay by phone? (可以手机支付吗？)

导购: Sure. Scan this code. (可以，扫这个码。)

中年女性: Could I have a bag, please? (能给我一个袋子吗？)

导购: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "电器城问参数别慌，跟导购对上几句。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "电器城", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'21751b59efd73f6473366292b3d5f9df','英语·夜市现场🌙','生成要求：9:16竖屏，15-20秒。中国夜市摊位，灯笼与小吃香。语速明快。风格：烟火气风。

画面描述：少女（约17-18岁，高马尾，运动外套与短裤，刚下体育课感）正在「购物/夜市」场景中，在「夜市」用英语完成沟通，主角是少女。配角为摊主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；摊主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, where can I find grilled skewers? (请问grilled skewers在哪里？)

摊主: On that shelf to your right. (在您右边那个架子上。)

少女: Do you have a smaller size? (有小一点的吗？)

摊主: Yes, let me check the stock. (有，我帮您看看库存。)

少女: How much is this? (这个多少钱？)

摊主: Twenty-five yuan. (二十五元。)

少女: Can I pay by phone? (可以手机支付吗？)

摊主: Sure. Scan this code. (可以，扫这个码。)

少女: Could I have a bag, please? (能给我一个袋子吗？)

摊主: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "夜市摊位砍价点单，烟火气拉满。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "夜市", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'830ae6ce74de1da3ba79517002126e91','夜市开口说英语🌙','生成要求：9:16竖屏，15-20秒。中国夜市摊位，灯笼与小吃香。语速明快。风格：烟火气风。

画面描述：中年男性（约36-46岁，干净短发，白衬衫卷袖与腕表，干练）正在「购物/夜市」场景中，在「夜市」用英语完成沟通，主角是中年男性。配角为摊主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；摊主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, where can I find grilled skewers? (请问grilled skewers在哪里？)

摊主: On that shelf to your right. (在您右边那个架子上。)

中年男性: Do you have a smaller size? (有小一点的吗？)

摊主: Yes, let me check the stock. (有，我帮您看看库存。)

中年男性: How much is this? (这个多少钱？)

摊主: Twenty-five yuan. (二十五元。)

中年男性: Can I pay by phone? (可以手机支付吗？)

摊主: Sure. Scan this code. (可以，扫这个码。)

中年男性: Could I have a bag, please? (能给我一个袋子吗？)

摊主: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "夜市摊位砍价点单，烟火气拉满。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "夜市", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'7b1057f0519fe3e8352706bb3ba93052','花店开口说英语🌸','生成要求：9:16竖屏，15-20秒。中国街边花店，鲜花与绿植。语速明快。风格：治愈生活风。

画面描述：少年（约17-18岁，黑框眼镜，针织毛衣与长裤，书卷气）正在「购物/花店」场景中，在「花店」用英语完成沟通，主角是少年。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, where can I find a small bouquet? (请问a small bouquet在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

少年: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

少年: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

少年: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

少年: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "花店买花问好不好养，温柔又实用。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "花店", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'2a2198b1ed13eed853a76e5680862ab2','老奶奶版花店英语🌸','生成要求：9:16竖屏，15-20秒。中国街边花店，鲜花与绿植。语速明快。风格：治愈生活风。

画面描述：老奶奶（约63-75岁，短发利落，唐装式盘扣外套，精神）正在「购物/花店」场景中，在「花店」用英语完成沟通，主角是老奶奶。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, where can I find a small bouquet? (请问a small bouquet在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

老奶奶: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

老奶奶: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

老奶奶: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

老奶奶: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "花店买花问好不好养，温柔又实用。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "花店", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ad36377ba6aeb461503aa15e51ac6097','跟读|水果店英语🛒','生成要求：9:16竖屏，15-20秒。中国水果店门口，果筐摆放整齐。语速明快。风格：人间烟火风。

画面描述：年轻女性（约23-29岁，微卷发，碎花连衣裙与凉鞋，温柔）正在「购物/水果店」场景中，在「水果店」用英语完成沟通，主角是年轻女性。配角为店主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；店主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, where can I find a bag of apples? (请问a bag of apples在哪里？)

店主: On that shelf to your right. (在您右边那个架子上。)

年轻女性: Do you have a smaller size? (有小一点的吗？)

店主: Yes, let me check the stock. (有，我帮您看看库存。)

年轻女性: How much is this? (这个多少钱？)

店主: Twenty-five yuan. (二十五元。)

年轻女性: Can I pay by phone? (可以手机支付吗？)

店主: Sure. Scan this code. (可以，扫这个码。)

年轻女性: Could I have a bag, please? (能给我一个袋子吗？)

店主: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "水果店问熟没熟、能不能切，超生活。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "水果店", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ee319128a7ae12304c5b806521677fd9','水果店情景英语🛒','生成要求：9:16竖屏，15-20秒。中国水果店门口，果筐摆放整齐。语速明快。风格：人间烟火风。

画面描述：老爷爷（约60-72岁，微秃白发，冲锋衣，可选手持登山杖，户外感）正在「购物/水果店」场景中，在「水果店」用英语完成沟通，主角是老爷爷。配角为店主。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；店主声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, where can I find a bag of apples? (请问a bag of apples在哪里？)

店主: On that shelf to your right. (在您右边那个架子上。)

老爷爷: Do you have a smaller size? (有小一点的吗？)

店主: Yes, let me check the stock. (有，我帮您看看库存。)

老爷爷: How much is this? (这个多少钱？)

店主: Twenty-five yuan. (二十五元。)

老爷爷: Can I pay by phone? (可以手机支付吗？)

店主: Sure. Scan this code. (可以，扫这个码。)

老爷爷: Could I have a bag, please? (能给我一个袋子吗？)

店主: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "水果店问熟没熟、能不能切，超生活。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "水果店", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b9609195c5dde1b606b3d00895b8d141','生活英语·特产店🛒','生成要求：9:16竖屏，15-20秒。中国景区特产店货架前。语速明快。风格：旅行购物风。

画面描述：年轻男性（约21-27岁，短刺头，牛仔夹克配T恤，随性）正在「购物/特产店」场景中，在「特产店」用英语完成沟通，主角是年轻男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, where can I find local snacks? (请问local snacks在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

年轻男性: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

年轻男性: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

年轻男性: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

年轻男性: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "景区特产店问价打包，旅游英语刚需。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "特产店", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9559a6569d65d6ef64858a72c42fb531','小女孩的特产店英语🛒','生成要求：9:16竖屏，15-20秒。中国景区特产店货架前。语速明快。风格：旅行购物风。

画面描述：小女孩（约6-10岁，羊角辫，碎花短袖裙，手拿折叠扇）正在「购物/特产店」场景中，在「特产店」用英语完成沟通，主角是小女孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, where can I find local snacks? (请问local snacks在哪里？)

店员: On that shelf to your right. (在您右边那个架子上。)

小女孩: Do you have a smaller size? (有小一点的吗？)

店员: Yes, let me check the stock. (有，我帮您看看库存。)

小女孩: How much is this? (这个多少钱？)

店员: Twenty-five yuan. (二十五元。)

小女孩: Can I pay by phone? (可以手机支付吗？)

店员: Sure. Scan this code. (可以，扫这个码。)

小女孩: Could I have a bag, please? (能给我一个袋子吗？)

店员: Of course. Have a nice day! (当然。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "购物", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "景区特产店问价打包，旅游英语刚需。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "购物", "sceneSub": "特产店", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9ec57b808b67211cb79ff1349acf19f4','老爷爷·网约车上车英语🚕','生成要求：9:16竖屏，15-20秒。中国小区门口网约车上车点。语速明快。风格：出行实用风。

画面描述：老爷爷（约65-80岁，花白背头，呢大衣与围巾，庄重）正在「交通出行/网约车上车」场景中，在「网约车上车」用英语完成沟通，主角是老爷爷。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, are you going to the city library? (你好，你是去the city library吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

老爷爷: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

老爷爷: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

老爷爷: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

老爷爷: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "网约车核对目的地、系安全带，上车就能说。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "网约车上车", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3b2c95e7b5eb3c7a253125de28f28c57','网约车上车英语跟读🚕','生成要求：9:16竖屏，15-20秒。中国小区门口网约车上车点。语速明快。风格：出行实用风。

画面描述：年轻女性（约20-28岁，黑长直，羽绒服内搭卫衣，日常街拍感）正在「交通出行/网约车上车」场景中，在「网约车上车」用英语完成沟通，主角是年轻女性。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, are you going to the city library? (你好，你是去the city library吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

年轻女性: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

年轻女性: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

年轻女性: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

年轻女性: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "网约车核对目的地、系安全带，上车就能说。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "网约车上车", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8ed77069cd1a436b4b98ead529be5eee','英语·高铁进站现场🚇','生成要求：9:16竖屏，15-20秒。中国高铁站安检口附近。语速明快。风格：出行记录风。

画面描述：小女孩（约7-10岁，短发刘海，素色Polo衫，干净清爽）正在「交通出行/高铁进站」场景中，在「高铁进站」用英语完成沟通，主角是小女孩。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, are you going to platform B? (你好，你是去platform B吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

小女孩: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

小女孩: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

小女孩: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

小女孩: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "高铁安检进站高频句，别只会跟着走。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "高铁进站", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'15eb1fc3a7d6116bba8ac8d70f023316','高铁进站开口说英语🚇','生成要求：9:16竖屏，15-20秒。中国高铁站安检口附近。语速明快。风格：出行记录风。

画面描述：年轻男性（约25-30岁，干净短发，深色Polo与休闲裤，稳重）正在「交通出行/高铁进站」场景中，在「高铁进站」用英语完成沟通，主角是年轻男性。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, are you going to platform B? (你好，你是去platform B吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

年轻男性: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

年轻男性: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

年轻男性: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

年轻男性: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "高铁安检进站高频句，别只会跟着走。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "高铁进站", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'2b4c656eb9b6f8bec5c91fd8ca182462','共享单车开口说英语🚲','生成要求：9:16竖屏，15-20秒。中国路边共享单车停放点。语速明快。风格：轻快都市风。

画面描述：小男孩（约6-10岁，平头，深蓝Polo与卡其短裤，认真听讲）正在「交通出行/共享单车」场景中，在「共享单车」用英语完成沟通，主角是小男孩。配角为路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, are you going to unlock this bike? (你好，你是去unlock this bike吗？)

路人: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

小男孩: How long will it take? (大概要多久？)

路人: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

小男孩: Could you drop me at the main entrance? (能在正门口停吗？)

路人: No problem. I''ll stop there. (没问题，我停在那儿。)

小男孩: Do I need to tap a card here? (这里需要刷卡吗？)

路人: Yes, or you can scan with your phone. (要，也可以手机扫。)

小男孩: Thanks for your help. (谢谢帮忙。)

路人: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "共享单车不会开？路边问一句就通。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "共享单车", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4a1809362998e8f45938edfdec7710cc','中年女性版共享单车英语🚲','生成要求：9:16竖屏，15-20秒。中国路边共享单车停放点。语速明快。风格：轻快都市风。

画面描述：中年女性（约35-48岁，马尾，运动外套与休闲裤，利落）正在「交通出行/共享单车」场景中，在「共享单车」用英语完成沟通，主角是中年女性。配角为路人。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；路人声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, are you going to unlock this bike? (你好，你是去unlock this bike吗？)

路人: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

中年女性: How long will it take? (大概要多久？)

路人: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

中年女性: Could you drop me at the main entrance? (能在正门口停吗？)

路人: No problem. I''ll stop there. (没问题，我停在那儿。)

中年女性: Do I need to tap a card here? (这里需要刷卡吗？)

路人: Yes, or you can scan with your phone. (要，也可以手机扫。)

中年女性: Thanks for your help. (谢谢帮忙。)

路人: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "共享单车不会开？路边问一句就通。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "共享单车", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d1d30ec3bf101cd3c2b45d30db4268cc','跟读|机场出租车英语🚇','生成要求：9:16竖屏，15-20秒。中国机场出租车候车点。语速明快。风格：旅行实用风。

画面描述：少女（约15-17岁，侧分长发，米色风衣与帆布袋，沉稳）正在「交通出行/机场出租车」场景中，在「机场出租车」用英语完成沟通，主角是少女。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, are you going to the downtown hotel? (你好，你是去the downtown hotel吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

少女: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

少女: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

少女: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

少女: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "机场出租车站排队上车，目的地说清楚。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "机场出租车", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1008a7399577e77c94880cf63ecb130d','机场出租车情景英语🚇','生成要求：9:16竖屏，15-20秒。中国机场出租车候车点。语速明快。风格：旅行实用风。

画面描述：中年男性（约40-50岁，花白鬓角，羊毛衫与长裤，慈和）正在「交通出行/机场出租车」场景中，在「机场出租车」用英语完成沟通，主角是中年男性。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, are you going to the downtown hotel? (你好，你是去the downtown hotel吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

中年男性: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

中年男性: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

中年男性: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

中年男性: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "机场出租车站排队上车，目的地说清楚。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "机场出租车", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ba44020b9e379670d807372e7e4c7a86','生活英语·公交刷卡🚇','生成要求：9:16竖屏，15-20秒。中国公交车前门刷卡处。语速明快。风格：日常通勤风。

画面描述：少年（约15-17岁，短碎发，球队球衣与运动裤，活力）正在「交通出行/公交刷卡」场景中，在「公交刷卡」用英语完成沟通，主角是少年。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, are you going to tap in? (你好，你是去tap in吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

少年: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

少年: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

少年: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

少年: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "公交前门刷卡上车，司机对话超短实用。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "公交刷卡", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'994caf976a4e880a7c1ac87b51fa6c8b','老奶奶的公交刷卡英语🚇','生成要求：9:16竖屏，15-20秒。中国公交车前门刷卡处。语速明快。风格：日常通勤风。

画面描述：老奶奶（约60-70岁，花白马尾松松扎起，围裙外披外套，烟火气）正在「交通出行/公交刷卡」场景中，在「公交刷卡」用英语完成沟通，主角是老奶奶。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, are you going to tap in? (你好，你是去tap in吗？)

司机: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

老奶奶: How long will it take? (大概要多久？)

司机: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

老奶奶: Could you drop me at the main entrance? (能在正门口停吗？)

司机: No problem. I''ll stop there. (没问题，我停在那儿。)

老奶奶: Do I need to tap a card here? (这里需要刷卡吗？)

司机: Yes, or you can scan with your phone. (要，也可以手机扫。)

老奶奶: Thanks for your help. (谢谢帮忙。)

司机: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "公交前门刷卡上车，司机对话超短实用。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "公交刷卡", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d33fc6a940528626d598a7840a3819e2','年轻女性·地铁换乘英语🚇','生成要求：9:16竖屏，15-20秒。中国地铁换乘通道，指示清晰。语速明快。风格：通勤实用风。

画面描述：年轻女性（约22-30岁，侧分长发，西装外套配阔腿裤，利落）正在「交通出行/地铁换乘」场景中，在「地铁换乘」用英语完成沟通，主角是年轻女性。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, are you going to Line 2? (你好，你是去Line 2吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

年轻女性: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

年轻女性: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

年轻女性: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

年轻女性: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "地铁换乘通道问路，指示牌看不懂也能问。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "地铁换乘", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'602c78a5f2878e593daeb38fc875cba2','地铁换乘英语跟读🚇','生成要求：9:16竖屏，15-20秒。中国地铁换乘通道，指示清晰。语速明快。风格：通勤实用风。

画面描述：老爷爷（约62-70岁，银发平头，格子衬衫卷袖，笑眯眯）正在「交通出行/地铁换乘」场景中，在「地铁换乘」用英语完成沟通，主角是老爷爷。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, are you going to Line 2? (你好，你是去Line 2吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

老爷爷: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

老爷爷: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

老爷爷: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

老爷爷: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "地铁换乘通道问路，指示牌看不懂也能问。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "地铁换乘", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6bdac979996779c500c44bbb82645e50','英语·商场停车场现场🚇','生成要求：9:16竖屏，15-20秒。中国商场地下停车场出口。语速明快。风格：都市生活风。

画面描述：年轻男性（约20-28岁，棒球帽，冲锋衣与运动鞋，户外感）正在「交通出行/商场停车场」场景中，在「商场停车场」用英语完成沟通，主角是年轻男性。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, are you going to the exit ramp? (你好，你是去the exit ramp吗？)

保安: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

年轻男性: How long will it take? (大概要多久？)

保安: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

年轻男性: Could you drop me at the main entrance? (能在正门口停吗？)

保安: No problem. I''ll stop there. (没问题，我停在那儿。)

年轻男性: Do I need to tap a card here? (这里需要刷卡吗？)

保安: Yes, or you can scan with your phone. (要，也可以手机扫。)

年轻男性: Thanks for your help. (谢谢帮忙。)

保安: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "停车场找出口，保安英语也能对上。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "商场停车场", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'de2e853e2d1b8157e17186bddfa9fecb','商场停车场开口说英语🚇','生成要求：9:16竖屏，15-20秒。中国商场地下停车场出口。语速明快。风格：都市生活风。

画面描述：小女孩（约6-9岁，侧边编发，牛仔短裤配白T恤，俏皮）正在「交通出行/商场停车场」场景中，在「商场停车场」用英语完成沟通，主角是小女孩。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, are you going to the exit ramp? (你好，你是去the exit ramp吗？)

保安: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

小女孩: How long will it take? (大概要多久？)

保安: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

小女孩: Could you drop me at the main entrance? (能在正门口停吗？)

保安: No problem. I''ll stop there. (没问题，我停在那儿。)

小女孩: Do I need to tap a card here? (这里需要刷卡吗？)

保安: Yes, or you can scan with your phone. (要，也可以手机扫。)

小女孩: Thanks for your help. (谢谢帮忙。)

保安: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "停车场找出口，保安英语也能对上。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "商场停车场", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'76a4ce3296f673d6dd47ccc0969f28aa','长途汽车站开口说英语🚇','生成要求：9:16竖屏，15-20秒。中国长途汽车站检票口。语速明快。风格：出行实用风。

画面描述：中年女性（约40-50岁，齐耳短发，深色羽绒服与围巾，温暖）正在「交通出行/长途汽车站」场景中，在「长途汽车站」用英语完成沟通，主角是中年女性。配角为检票员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；检票员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, are you going to the ticket gate? (你好，你是去the ticket gate吗？)

检票员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

中年女性: How long will it take? (大概要多久？)

检票员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

中年女性: Could you drop me at the main entrance? (能在正门口停吗？)

检票员: No problem. I''ll stop there. (没问题，我停在那儿。)

中年女性: Do I need to tap a card here? (这里需要刷卡吗？)

检票员: Yes, or you can scan with your phone. (要，也可以手机扫。)

中年女性: Thanks for your help. (谢谢帮忙。)

检票员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "长途站检票口确认车次，别再干着急。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "长途汽车站", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'29b8289b5e0ec0a96b30b5d7751052b6','小男孩版长途汽车站英语🚇','生成要求：9:16竖屏，15-20秒。中国长途汽车站检票口。语速明快。风格：出行实用风。

画面描述：小男孩（约8-9岁，轻微卷发，卫衣帽子半戴，轻松自然）正在「交通出行/长途汽车站」场景中，在「长途汽车站」用英语完成沟通，主角是小男孩。配角为检票员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；检票员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, are you going to the ticket gate? (你好，你是去the ticket gate吗？)

检票员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

小男孩: How long will it take? (大概要多久？)

检票员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

小男孩: Could you drop me at the main entrance? (能在正门口停吗？)

检票员: No problem. I''ll stop there. (没问题，我停在那儿。)

小男孩: Do I need to tap a card here? (这里需要刷卡吗？)

检票员: Yes, or you can scan with your phone. (要，也可以手机扫。)

小男孩: Thanks for your help. (谢谢帮忙。)

检票员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "长途站检票口确认车次，别再干着急。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "长途汽车站", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'7464f636cb594d43a19c02f6b5af5582','跟读|轮渡码头英语🚇','生成要求：9:16竖屏，15-20秒。中国轮渡码头候船厅。语速明快。风格：旅行治愈风。

画面描述：中年男性（约35-48岁，短刺头，牛仔外套与工装裤，接地气）正在「交通出行/轮渡码头」场景中，在「轮渡码头」用英语完成沟通，主角是中年男性。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, are you going to the ferry to the island? (你好，你是去the ferry to the island吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

中年男性: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

中年男性: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

中年男性: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

中年男性: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "轮渡候船沟通，出行场景补齐。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "轮渡码头", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4992b29a233964c3f1989df01f600b14','轮渡码头情景英语🚇','生成要求：9:16竖屏，15-20秒。中国轮渡码头候船厅。语速明快。风格：旅行治愈风。

画面描述：少女（约16-18岁，双马尾，学院风百褶裙，笑意温柔）正在「交通出行/轮渡码头」场景中，在「轮渡码头」用英语完成沟通，主角是少女。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, are you going to the ferry to the island? (你好，你是去the ferry to the island吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

少女: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

少女: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

少女: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

少女: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "轮渡候船沟通，出行场景补齐。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "轮渡码头", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'fe4a1696447095554342320eb7b0d165','生活英语·火车站出站🚇','生成要求：9:16竖屏，15-20秒。中国火车站出站口。语速明快。风格：出行记录风。

画面描述：老奶奶（约65-80岁，白发梳齐，深色毛呢大衣，慢条斯理）正在「交通出行/火车站出站」场景中，在「火车站出站」用英语完成沟通，主角是老奶奶。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, are you going to the south exit? (你好，你是去the south exit吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

老奶奶: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

老奶奶: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

老奶奶: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

老奶奶: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火车站出站口问接驳，落地就会说。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "火车站出站", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c64a9aa63a990b9e04df05decfff2671','少年的火车站出站英语🚇','生成要求：9:16竖屏，15-20秒。中国火车站出站口。语速明快。风格：出行记录风。

画面描述：少年（约16-18岁，干净短发，浅色衬衫卷袖，礼貌微笑）正在「交通出行/火车站出站」场景中，在「火车站出站」用英语完成沟通，主角是少年。配角为工作人员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；工作人员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, are you going to the south exit? (你好，你是去the south exit吗？)

工作人员: Yes. Please get in and fasten your seatbelt. (是的，请上车系好安全带。)

少年: How long will it take? (大概要多久？)

工作人员: Around twenty minutes in this traffic. (按现在路况大概二十分钟。)

少年: Could you drop me at the main entrance? (能在正门口停吗？)

工作人员: No problem. I''ll stop there. (没问题，我停在那儿。)

少年: Do I need to tap a card here? (这里需要刷卡吗？)

工作人员: Yes, or you can scan with your phone. (要，也可以手机扫。)

少年: Thanks for your help. (谢谢帮忙。)

工作人员: You''re welcome. Safe travels! (不客气，一路顺利！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "交通出行", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "火车站出站口问接驳，落地就会说。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "交通出行", "sceneSub": "火车站出站", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'22daa559ac182b422458ef84bd861ec9','少女·前台入住英语🏨','生成要求：9:16竖屏，15-20秒。中国商务酒店前台。语速明快。风格：旅行实用风。

画面描述：少女（约15-18岁，长直发，校服白衬衫深蓝裙，礼貌大方）正在「酒店住宿/前台入住」场景中，在「前台入住」用英语完成沟通，主角是少女。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

少女: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

少女: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

少女: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

少女: Could you help me with check in? (能帮我check in吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店前台入住：证件、早餐、安静房一次问全。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "前台入住", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'13a619d92e8a71f8282c6175473a0b1a','前台入住英语跟读🏨','生成要求：9:16竖屏，15-20秒。中国商务酒店前台。语速明快。风格：旅行实用风。

画面描述：中年男性（约35-50岁，短发微白，衬衫与夹克，可靠亲切）正在「酒店住宿/前台入住」场景中，在「前台入住」用英语完成沟通，主角是中年男性。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

中年男性: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

中年男性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

中年男性: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

中年男性: Could you help me with check in? (能帮我check in吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店前台入住：证件、早餐、安静房一次问全。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "前台入住", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'307f1e6c43eb0f888038d47cb0e126b0','英语·叫醒服务现场🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房内，床头电话。语速明快。风格：温馨旅行风。

画面描述：少年（约15-18岁，短发清爽，运动服套装，礼貌稳重）正在「酒店住宿/叫醒服务」场景中，在「叫醒服务」用英语完成沟通，主角是少年。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

少年: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

少年: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

少年: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

少年: Could you help me with a wake-up call? (能帮我a wake-up call吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客房打电话要叫醒，这几句就够。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "叫醒服务", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a29db874fe52f5d81c11255b50255533','叫醒服务开口说英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房内，床头电话。语速明快。风格：温馨旅行风。

画面描述：老奶奶（约60+岁，花白短发，碎花布衣与薄外套，笑容温和）正在「酒店住宿/叫醒服务」场景中，在「叫醒服务」用英语完成沟通，主角是老奶奶。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

老奶奶: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

老奶奶: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

老奶奶: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

老奶奶: Could you help me with a wake-up call? (能帮我a wake-up call吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客房打电话要叫醒，这几句就够。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "叫醒服务", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'99f530bbc7231ce5ad93ff218662cf41','洗衣服务开口说英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房门口，洗衣袋。语速明快。风格：服务实用风。

画面描述：年轻女性（约20-30岁，披肩长发，米色风衣与乐福鞋，自信自然）正在「酒店住宿/洗衣服务」场景中，在「洗衣服务」用英语完成沟通，主角是年轻女性。配角为客房服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；客房服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I have a reservation under my name. (你好，我有预订。)

客房服务员: May I see your ID, please? (请出示一下证件好吗？)

年轻女性: Is breakfast included? (含早餐吗？)

客房服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

年轻女性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

客房服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

年轻女性: What time is check-out? (退房是几点？)

客房服务员: Before twelve noon. (中午十二点前。)

年轻女性: Could you help me with laundry service? (能帮我laundry service吗？)

客房服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店洗衣怎么交代，客房门口就能说。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "洗衣服务", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9f06b99a25160ddd1f0c450b8aed7fbe','老爷爷版洗衣服务英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房门口，洗衣袋。语速明快。风格：服务实用风。

画面描述：老爷爷（约60+岁，花白短发，衬衫与马甲，慈祥耐心）正在「酒店住宿/洗衣服务」场景中，在「洗衣服务」用英语完成沟通，主角是老爷爷。配角为客房服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；客房服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I have a reservation under my name. (你好，我有预订。)

客房服务员: May I see your ID, please? (请出示一下证件好吗？)

老爷爷: Is breakfast included? (含早餐吗？)

客房服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

老爷爷: Could I get a quiet room, please? (能给我安静一点的房间吗？)

客房服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

老爷爷: What time is check-out? (退房是几点？)

客房服务员: Before twelve noon. (中午十二点前。)

老爷爷: Could you help me with laundry service? (能帮我laundry service吗？)

客房服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "酒店洗衣怎么交代，客房门口就能说。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "洗衣服务", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'de73f678dfc7c0e34f1e841101ca8504','跟读|客房送物英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房门口走廊。语速明快。风格：服务记录风。

画面描述：年轻男性（约20-30岁，短发整齐，休闲西装外套，干练友善）正在「酒店住宿/客房送物」场景中，在「客房送物」用英语完成沟通，主角是年轻男性。配角为客房服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；客房服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I have a reservation under my name. (你好，我有预订。)

客房服务员: May I see your ID, please? (请出示一下证件好吗？)

年轻男性: Is breakfast included? (含早餐吗？)

客房服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

年轻男性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

客房服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

年轻男性: What time is check-out? (退房是几点？)

客房服务员: Before twelve noon. (中午十二点前。)

年轻男性: Could you help me with extra towels? (能帮我extra towels吗？)

客房服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "要水要毛巾怎么说，礼貌又清楚。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "客房送物", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a27e61afa96913e054c48b312bf6a94d','客房送物情景英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房门口走廊。语速明快。风格：服务记录风。

画面描述：小女孩（约6-10岁，马尾辫配粉色发卡，浅色短袖连衣裙，活泼自然）正在「酒店住宿/客房送物」场景中，在「客房送物」用英语完成沟通，主角是小女孩。配角为客房服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；客房服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I have a reservation under my name. (你好，我有预订。)

客房服务员: May I see your ID, please? (请出示一下证件好吗？)

小女孩: Is breakfast included? (含早餐吗？)

客房服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

小女孩: Could I get a quiet room, please? (能给我安静一点的房间吗？)

客房服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

小女孩: What time is check-out? (退房是几点？)

客房服务员: Before twelve noon. (中午十二点前。)

小女孩: Could you help me with extra towels? (能帮我extra towels吗？)

客房服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "要水要毛巾怎么说，礼貌又清楚。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "客房送物", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'f900aa905f75829254693d0efff1d3c6','生活英语·酒店健身房🏨','生成要求：9:16竖屏，15-20秒。中国酒店健身房入口。语速明快。风格：健康生活风。

画面描述：中年女性（约35-50岁，齐肩短发，得体风衣与平底鞋，沉稳耐心）正在「酒店住宿/酒店健身房」场景中，在「酒店健身房」用英语完成沟通，主角是中年女性。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

中年女性: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

中年女性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

中年女性: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

中年女性: Could you help me with the gym access? (能帮我the gym access吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "问健身房开放时间，住店也能练口语。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "酒店健身房", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'5abe764633fc4e86d5fe244375461ae2','小男孩的酒店健身房英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店健身房入口。语速明快。风格：健康生活风。

画面描述：小男孩（约6-10岁，短发，蓝白条纹T恤与运动鞋，好奇爱提问）正在「酒店住宿/酒店健身房」场景中，在「酒店健身房」用英语完成沟通，主角是小男孩。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

小男孩: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

小男孩: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

小男孩: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

小男孩: Could you help me with the gym access? (能帮我the gym access吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "问健身房开放时间，住店也能练口语。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "酒店健身房", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ecaa71f9242bd9a8a9ee206477a48837','中年男性·酒店早餐英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店自助早餐区。语速明快。风格：旅行烟火风。

画面描述：中年男性（约38-48岁，寸头，Polo衫与卡其裤，朴实）正在「酒店住宿/酒店早餐」场景中，在「酒店早餐」用英语完成沟通，主角是中年男性。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I have a reservation under my name. (你好，我有预订。)

服务员: May I see your ID, please? (请出示一下证件好吗？)

中年男性: Is breakfast included? (含早餐吗？)

服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

中年男性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

中年男性: What time is check-out? (退房是几点？)

服务员: Before twelve noon. (中午十二点前。)

中年男性: Could you help me with breakfast hours? (能帮我breakfast hours吗？)

服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "自助早餐区沟通，别只会微笑。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "酒店早餐", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'38ad290ece47dd5627b999d900561be4','酒店早餐英语跟读🏨','生成要求：9:16竖屏，15-20秒。中国酒店自助早餐区。语速明快。风格：旅行烟火风。

画面描述：少女（约16-18岁，低马尾，浅色针织开衫与牛仔裤，安静文雅）正在「酒店住宿/酒店早餐」场景中，在「酒店早餐」用英语完成沟通，主角是少女。配角为服务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；服务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I have a reservation under my name. (你好，我有预订。)

服务员: May I see your ID, please? (请出示一下证件好吗？)

少女: Is breakfast included? (含早餐吗？)

服务员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

少女: Could I get a quiet room, please? (能给我安静一点的房间吗？)

服务员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

少女: What time is check-out? (退房是几点？)

服务员: Before twelve noon. (中午十二点前。)

少女: Could you help me with breakfast hours? (能帮我breakfast hours吗？)

服务员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "自助早餐区沟通，别只会微笑。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "酒店早餐", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e206743f124fd3831f455e780a7107dc','英语·延期退房现场🏨','生成要求：9:16竖屏，15-20秒。中国酒店前台办手续处。语速明快。风格：实用沟通风。

画面描述：老奶奶（约62-75岁，盘发插簪，深色棉袄与布鞋，慈祥）正在「酒店住宿/延期退房」场景中，在「延期退房」用英语完成沟通，主角是老奶奶。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

老奶奶: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

老奶奶: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

老奶奶: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

老奶奶: Could you help me with a late check-out? (能帮我a late check-out吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "想晚点退房？前台这几句超管用。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "延期退房", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'be76cd5836524ee4072f440265160132','延期退房开口说英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店前台办手续处。语速明快。风格：实用沟通风。

画面描述：少年（约16-18岁，微乱短发，连帽卫衣与牛仔裤，随性）正在「酒店住宿/延期退房」场景中，在「延期退房」用英语完成沟通，主角是少年。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

少年: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

少年: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

少年: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

少年: Could you help me with a late check-out? (能帮我a late check-out吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "想晚点退房？前台这几句超管用。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "延期退房", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8cc5fba7e0c895726afa3588742cb454','寻找行李开口说英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店大堂行李处。语速明快。风格：旅行实用风。

画面描述：老爷爷（约62-75岁，白发寸头，中山装式外套，精神矍铄）正在「酒店住宿/寻找行李」场景中，在「寻找行李」用英语完成沟通，主角是老爷爷。配角为行李员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；行李员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I have a reservation under my name. (你好，我有预订。)

行李员: May I see your ID, please? (请出示一下证件好吗？)

老爷爷: Is breakfast included? (含早餐吗？)

行李员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

老爷爷: Could I get a quiet room, please? (能给我安静一点的房间吗？)

行李员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

老爷爷: What time is check-out? (退房是几点？)

行李员: Before twelve noon. (中午十二点前。)

老爷爷: Could you help me with find my luggage? (能帮我find my luggage吗？)

行李员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "大堂找行李别慌，行李员对话跟读。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "寻找行李", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'62b73156a10460f19da0e37b7e0617be','年轻女性版寻找行李英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店大堂行李处。语速明快。风格：旅行实用风。

画面描述：年轻女性（约22-28岁，低盘发，白衬衫配西裤，干练通勤）正在「酒店住宿/寻找行李」场景中，在「寻找行李」用英语完成沟通，主角是年轻女性。配角为行李员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；行李员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I have a reservation under my name. (你好，我有预订。)

行李员: May I see your ID, please? (请出示一下证件好吗？)

年轻女性: Is breakfast included? (含早餐吗？)

行李员: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

年轻女性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

行李员: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

年轻女性: What time is check-out? (退房是几点？)

行李员: Before twelve noon. (中午十二点前。)

年轻女性: Could you help me with find my luggage? (能帮我find my luggage吗？)

行李员: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "大堂找行李别慌，行李员对话跟读。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "寻找行李", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9cc980a90be01940dab905df0c38abb1','跟读|连接WiFi英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房书桌旁。语速明快。风格：现代旅行风。

画面描述：小女孩（约6-10岁，戴遮阳帽与小书包，格子短袖短裤，好奇张望）正在「酒店住宿/连接WiFi」场景中，在「连接WiFi」用英语完成沟通，主角是小女孩。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

小女孩: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

小女孩: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

小女孩: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

小女孩: Could you help me with the Wi-Fi password? (能帮我the Wi-Fi password吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客房连不上网？问前台密码这样说。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "连接WiFi", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e59f9458bb94d10b0774e3e0e054e3b2','连接WiFi情景英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店客房书桌旁。语速明快。风格：现代旅行风。

画面描述：年轻男性（约22-28岁，微长碎发，连帽卫衣与牛仔裤，轻松）正在「酒店住宿/连接WiFi」场景中，在「连接WiFi」用英语完成沟通，主角是年轻男性。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I have a reservation under my name. (你好，我有预订。)

前台: May I see your ID, please? (请出示一下证件好吗？)

年轻男性: Is breakfast included? (含早餐吗？)

前台: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

年轻男性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

前台: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

年轻男性: What time is check-out? (退房是几点？)

前台: Before twelve noon. (中午十二点前。)

年轻男性: Could you help me with the Wi-Fi password? (能帮我the Wi-Fi password吗？)

前台: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客房连不上网？问前台密码这样说。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "连接WiFi", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4264356dec9e6a3fb0bbb7abddf5b260','生活英语·服务投诉🏨','生成要求：9:16竖屏，15-20秒。中国酒店大堂经理台。语速明快。风格：理性沟通风。

画面描述：小男孩（约7-9岁，戴鸭舌帽，迷彩短裤与背心，活力十足）正在「酒店住宿/服务投诉」场景中，在「服务投诉」用英语完成沟通，主角是小男孩。配角为大堂经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；大堂经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I have a reservation under my name. (你好，我有预订。)

大堂经理: May I see your ID, please? (请出示一下证件好吗？)

小男孩: Is breakfast included? (含早餐吗？)

大堂经理: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

小男孩: Could I get a quiet room, please? (能给我安静一点的房间吗？)

大堂经理: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

小男孩: What time is check-out? (退房是几点？)

大堂经理: Before twelve noon. (中午十二点前。)

小男孩: Could you help me with a room issue? (能帮我a room issue吗？)

大堂经理: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "有问题找大堂经理，表达清楚不尴尬。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "服务投诉", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a3ac454608dc2ba64bc5c136c544aa5c','中年女性的服务投诉英语🏨','生成要求：9:16竖屏，15-20秒。中国酒店大堂经理台。语速明快。风格：理性沟通风。

画面描述：中年女性（约38-48岁，微卷中长发，针织衫配长裙，温和）正在「酒店住宿/服务投诉」场景中，在「服务投诉」用英语完成沟通，主角是中年女性。配角为大堂经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；大堂经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I have a reservation under my name. (你好，我有预订。)

大堂经理: May I see your ID, please? (请出示一下证件好吗？)

中年女性: Is breakfast included? (含早餐吗？)

大堂经理: Yes, from seven to ten on the second floor. (含，二楼七点到十点。)

中年女性: Could I get a quiet room, please? (能给我安静一点的房间吗？)

大堂经理: Sure. I''ll put you on a higher floor. (好的，给您安排高一点的楼层。)

中年女性: What time is check-out? (退房是几点？)

大堂经理: Before twelve noon. (中午十二点前。)

中年女性: Could you help me with a room issue? (能帮我a room issue吗？)

大堂经理: Of course. We''ll handle it right away. (当然，我们马上处理。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "酒店住宿", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "有问题找大堂经理，表达清楚不尴尬。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "酒店住宿", "sceneSub": "服务投诉", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9a35e96ae03632cff1480811829022cc','年轻男性·医院挂号英语🏥','生成要求：9:16竖屏，15-20秒。中国医院自助挂号机旁。语速明快。风格：实用就医风。

画面描述：年轻男性（约24-30岁，侧分头，白衬衫卷袖与斜挎包，利落）正在「就医问诊/医院挂号」场景中，在「医院挂号」用英语完成沟通，主角是年轻男性。配角为导诊员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；导诊员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

导诊员: Which department do you need? (您要挂哪个科？)

年轻男性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

导诊员: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

年轻男性: Do I need any tests? (需要做什么检查吗？)

导诊员: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

年轻男性: How should I take this medicine? (这个药怎么吃？)

导诊员: Twice a day after meals. (一天两次，饭后服用。)

年轻男性: Where do I pay? (在哪里缴费？)

导诊员: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "医院挂号选科室，第一次也能说出口。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "医院挂号", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1c99a9f6294f27508b4bf82043ea6835','医院挂号英语跟读🏥','生成要求：9:16竖屏，15-20秒。中国医院自助挂号机旁。语速明快。风格：实用就医风。

画面描述：小女孩（约7-9岁，齐肩发，条纹T恤配背带裤，笑容明亮）正在「就医问诊/医院挂号」场景中，在「医院挂号」用英语完成沟通，主角是小女孩。配角为导诊员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；导诊员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

导诊员: Which department do you need? (您要挂哪个科？)

小女孩: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

导诊员: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

小女孩: Do I need any tests? (需要做什么检查吗？)

导诊员: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

小女孩: How should I take this medicine? (这个药怎么吃？)

导诊员: Twice a day after meals. (一天两次，饭后服用。)

小女孩: Where do I pay? (在哪里缴费？)

导诊员: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "医院挂号选科室，第一次也能说出口。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "医院挂号", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a79707bf3e07024baa2c1d32635db82c','英语·门诊问诊现场🏥','生成要求：9:16竖屏，15-20秒。中国医院诊室，医生坐诊。语速明快。风格：温和实用风。

画面描述：中年女性（约35-45岁，利落短发，衬衫与西裤套装，干练）正在「就医问诊/门诊问诊」场景中，在「门诊问诊」用英语完成沟通，主角是中年女性。配角为医生。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；医生声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

医生: Which department do you need? (您要挂哪个科？)

中年女性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

医生: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

中年女性: Do I need any tests? (需要做什么检查吗？)

医生: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

中年女性: How should I take this medicine? (这个药怎么吃？)

医生: Twice a day after meals. (一天两次，饭后服用。)

中年女性: Where do I pay? (在哪里缴费？)

医生: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "诊室描述喉咙痛低烧，医生听得懂。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "门诊问诊", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'39d1471ee5711cf88044156b67d8cba8','门诊问诊开口说英语🏥','生成要求：9:16竖屏，15-20秒。中国医院诊室，医生坐诊。语速明快。风格：温和实用风。

画面描述：小男孩（约6-8岁，圆寸头，校服式白衬衫卷袖，书包斜挎）正在「就医问诊/门诊问诊」场景中，在「门诊问诊」用英语完成沟通，主角是小男孩。配角为医生。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；医生声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

医生: Which department do you need? (您要挂哪个科？)

小男孩: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

医生: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

小男孩: Do I need any tests? (需要做什么检查吗？)

医生: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

小男孩: How should I take this medicine? (这个药怎么吃？)

医生: Twice a day after meals. (一天两次，饭后服用。)

小男孩: Where do I pay? (在哪里缴费？)

医生: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "诊室描述喉咙痛低烧，医生听得懂。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "门诊问诊", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'36d9a3a7bb257923f73d7d47085a09f3','药房取药开口说英语🏥','生成要求：9:16竖屏，15-20秒。中国医院药房窗口。语速明快。风格：就医实用风。

画面描述：中年男性（约40-50岁，黑框眼镜，深色西装外套便装化，稳重）正在「就医问诊/药房取药」场景中，在「药房取药」用英语完成沟通，主角是中年男性。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

药师: Which department do you need? (您要挂哪个科？)

中年男性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

药师: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

中年男性: Do I need any tests? (需要做什么检查吗？)

药师: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

中年男性: How should I take this medicine? (这个药怎么吃？)

药师: Twice a day after meals. (一天两次，饭后服用。)

中年男性: Where do I pay? (在哪里缴费？)

药师: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "药房窗口取药问用法，安全又实用。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "药房取药", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'83d93c5369e24634c2324c07e410e73b','少女版药房取药英语🏥','生成要求：9:16竖屏，15-20秒。中国医院药房窗口。语速明快。风格：就医实用风。

画面描述：少女（约15-17岁，齐肩黑发，卫衣配帆布鞋，校园休闲感）正在「就医问诊/药房取药」场景中，在「药房取药」用英语完成沟通，主角是少女。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

药师: Which department do you need? (您要挂哪个科？)

少女: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

药师: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

少女: Do I need any tests? (需要做什么检查吗？)

药师: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

少女: How should I take this medicine? (这个药怎么吃？)

药师: Twice a day after meals. (一天两次，饭后服用。)

少女: Where do I pay? (在哪里缴费？)

药师: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "药房窗口取药问用法，安全又实用。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "药房取药", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'117683bf8fb17305568a08c826deced9','跟读|检查化验英语🏥','生成要求：9:16竖屏，15-20秒。中国医院检验科采血窗口。语速明快。风格：冷静实用风。

画面描述：老奶奶（约60-70岁，齐耳白发，针织开衫与长裤，清爽）正在「就医问诊/检查化验」场景中，在「检查化验」用英语完成沟通，主角是老奶奶。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

老奶奶: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

老奶奶: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

老奶奶: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

老奶奶: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "检验科采血前沟通，紧张也能说清。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "检查化验", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'fbd0d82d84bb2f82b060ac1319206746','检查化验情景英语🏥','生成要求：9:16竖屏，15-20秒。中国医院检验科采血窗口。语速明快。风格：冷静实用风。

画面描述：少年（约15-17岁，侧分头，白衬衫校服裤，书包双肩）正在「就医问诊/检查化验」场景中，在「检查化验」用英语完成沟通，主角是少年。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

少年: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

少年: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

少年: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

少年: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "检验科采血前沟通，紧张也能说清。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "检查化验", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'877ef41e57dc8bc8562d45b4437ab5f0','生活英语·急诊分诊🏥','生成要求：9:16竖屏，15-20秒。中国医院急诊分诊台。语速明快。风格：紧张实用风。

画面描述：老爷爷（约60-70岁，银框眼镜，羊毛衫与长裤，温和）正在「就医问诊/急诊分诊」场景中，在「急诊分诊」用英语完成沟通，主角是老爷爷。配角为分诊护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；分诊护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

分诊护士: Which department do you need? (您要挂哪个科？)

老爷爷: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

分诊护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

老爷爷: Do I need any tests? (需要做什么检查吗？)

分诊护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

老爷爷: How should I take this medicine? (这个药怎么吃？)

分诊护士: Twice a day after meals. (一天两次，饭后服用。)

老爷爷: Where do I pay? (在哪里缴费？)

分诊护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "急诊分诊台说明情况，关键时刻用得上。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "急诊分诊", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'05000716977cab3f51d44e65bea143ca','年轻女性的急诊分诊英语🏥','生成要求：9:16竖屏，15-20秒。中国医院急诊分诊台。语速明快。风格：紧张实用风。

画面描述：年轻女性（约20-26岁，齐肩波浪发，针织衫与牛仔裙，柔和）正在「就医问诊/急诊分诊」场景中，在「急诊分诊」用英语完成沟通，主角是年轻女性。配角为分诊护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；分诊护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

分诊护士: Which department do you need? (您要挂哪个科？)

年轻女性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

分诊护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

年轻女性: Do I need any tests? (需要做什么检查吗？)

分诊护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

年轻女性: How should I take this medicine? (这个药怎么吃？)

分诊护士: Twice a day after meals. (一天两次，饭后服用。)

年轻女性: Where do I pay? (在哪里缴费？)

分诊护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "急诊分诊台说明情况，关键时刻用得上。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "急诊分诊", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1813506ac11266a286b709243e6dace5','小女孩·儿科候诊英语🏥','生成要求：9:16竖屏，15-20秒。中国医院儿科候诊区。语速明快。风格：温馨实用风。

画面描述：小女孩（约6-8岁，双麻花辫，浅色防晒衣与运动鞋，蹦跳感）正在「就医问诊/儿科候诊」场景中，在「儿科候诊」用英语完成沟通，主角是小女孩。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

小女孩: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

小女孩: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

小女孩: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

小女孩: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "儿科候诊区对话，家长场景超贴近。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "儿科候诊", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'f87fe7f93b2c4d21c49a0987cfa53e91','儿科候诊英语跟读🏥','生成要求：9:16竖屏，15-20秒。中国医院儿科候诊区。语速明快。风格：温馨实用风。

画面描述：年轻男性（约20-26岁，寸头，运动外套与跑鞋，精神）正在「就医问诊/儿科候诊」场景中，在「儿科候诊」用英语完成沟通，主角是年轻男性。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

年轻男性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

年轻男性: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

年轻男性: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

年轻男性: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "儿科候诊区对话，家长场景超贴近。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "儿科候诊", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a528ed0128ac3d7f8d43fa0326565f41','英语·窗口缴费现场🏥','生成要求：9:16竖屏，15-20秒。中国医院收费窗口。语速明快。风格：就医流程风。

画面描述：小男孩（约8-10岁，刺猬头，红色运动套装，额头微汗）正在「就医问诊/窗口缴费」场景中，在「窗口缴费」用英语完成沟通，主角是小男孩。配角为收费员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；收费员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

收费员: Which department do you need? (您要挂哪个科？)

小男孩: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

收费员: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

小男孩: Do I need any tests? (需要做什么检查吗？)

收费员: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

小男孩: How should I take this medicine? (这个药怎么吃？)

收费员: Twice a day after meals. (一天两次，饭后服用。)

小男孩: Where do I pay? (在哪里缴费？)

收费员: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "收费窗口缴费确认，别只会递手机。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "窗口缴费", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'72f0ee55e0d1ce104d8aad9c0fd71371','窗口缴费开口说英语🏥','生成要求：9:16竖屏，15-20秒。中国医院收费窗口。语速明快。风格：就医流程风。

画面描述：中年女性（约40-50岁，低盘发，丝巾与大衣，端庄）正在「就医问诊/窗口缴费」场景中，在「窗口缴费」用英语完成沟通，主角是中年女性。配角为收费员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；收费员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

收费员: Which department do you need? (您要挂哪个科？)

中年女性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

收费员: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

中年女性: Do I need any tests? (需要做什么检查吗？)

收费员: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

中年女性: How should I take this medicine? (这个药怎么吃？)

收费员: Twice a day after meals. (一天两次，饭后服用。)

中年女性: Where do I pay? (在哪里缴费？)

收费员: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "收费窗口缴费确认，别只会递手机。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "窗口缴费", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'2be6be90964a426b71ae736f1a0e8fd7','住院咨询开口说英语🏥','生成要求：9:16竖屏，15-20秒。中国医院住院处咨询台。语速明快。风格：稳重实用风。

画面描述：少女（约16-18岁，半扎发，浅色连衣裙与小挎包，清爽）正在「就医问诊/住院咨询」场景中，在「住院咨询」用英语完成沟通，主角是少女。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

少女: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

少女: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

少女: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

少女: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "住院处咨询流程，问清楚再办事。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "住院咨询", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e060c10af7af228d856f36f38d696dfd','中年男性版住院咨询英语🏥','生成要求：9:16竖屏，15-20秒。中国医院住院处咨询台。语速明快。风格：稳重实用风。

画面描述：中年男性（约35-45岁，中分短发，格子衬衫与帆布鞋，随和）正在「就医问诊/住院咨询」场景中，在「住院咨询」用英语完成沟通，主角是中年男性。配角为护士。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；护士声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

护士: Which department do you need? (您要挂哪个科？)

中年男性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

护士: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

中年男性: Do I need any tests? (需要做什么检查吗？)

护士: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

中年男性: How should I take this medicine? (这个药怎么吃？)

护士: Twice a day after meals. (一天两次，饭后服用。)

中年男性: Where do I pay? (在哪里缴费？)

护士: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "住院处咨询流程，问清楚再办事。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "住院咨询", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d12984ccbf491ee1f876d6dc1a66ce32','跟读|体检中心英语🏥','生成要求：9:16竖屏，15-20秒。中国体检中心前台。语速明快。风格：健康生活风。

画面描述：少年（约16-18岁，寸头，深色夹克配帆布鞋，干练）正在「就医问诊/体检中心」场景中，在「体检中心」用英语完成沟通，主角是少年。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

前台: Which department do you need? (您要挂哪个科？)

少年: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

前台: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

少年: Do I need any tests? (需要做什么检查吗？)

前台: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

少年: How should I take this medicine? (这个药怎么吃？)

前台: Twice a day after meals. (一天两次，饭后服用。)

少年: Where do I pay? (在哪里缴费？)

前台: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "体检中心前台报到，开口不慌。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "体检中心", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c5e605f40454abe5fbec350dfe999fb2','体检中心情景英语🏥','生成要求：9:16竖屏，15-20秒。中国体检中心前台。语速明快。风格：健康生活风。

画面描述：老奶奶（约65-78岁，头巾轻裹，格子外套与布包，家常）正在「就医问诊/体检中心」场景中，在「体检中心」用英语完成沟通，主角是老奶奶。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

前台: Which department do you need? (您要挂哪个科？)

老奶奶: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

前台: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

老奶奶: Do I need any tests? (需要做什么检查吗？)

前台: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

老奶奶: How should I take this medicine? (这个药怎么吃？)

前台: Twice a day after meals. (一天两次，饭后服用。)

老奶奶: Where do I pay? (在哪里缴费？)

前台: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "体检中心前台报到，开口不慌。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "体检中心", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'814eab74d1b8b01e329b0b2a17f070fb','生活英语·医院药房🏥','生成要求：9:16竖屏，15-20秒。中国社区医院药房柜台。语速明快。风格：邻里实用风。

画面描述：年轻女性（约24-30岁，高马尾，运动夹克与瑜伽裤，轻快）正在「就医问诊/医院药房」场景中，在「医院药房」用英语完成沟通，主角是年轻女性。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

药师: Which department do you need? (您要挂哪个科？)

年轻女性: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

药师: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

年轻女性: Do I need any tests? (需要做什么检查吗？)

药师: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

年轻女性: How should I take this medicine? (这个药怎么吃？)

药师: Twice a day after meals. (一天两次，饭后服用。)

年轻女性: Where do I pay? (在哪里缴费？)

药师: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "社区药房问药，日常健康英语。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "医院药房", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b9f332f9efef9c91f87299facda6aae6','老爷爷的医院药房英语🏥','生成要求：9:16竖屏，15-20秒。中国社区医院药房柜台。语速明快。风格：邻里实用风。

画面描述：老爷爷（约65-78岁，花白寸发，深蓝夹克与布帽，朴实）正在「就医问诊/医院药房」场景中，在「医院药房」用英语完成沟通，主角是老爷爷。配角为药师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；药师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I need to register for a checkup. (你好，我要挂号做检查。)

药师: Which department do you need? (您要挂哪个科？)

老爷爷: I have a sore throat and a mild fever. (我喉咙痛，还有点低烧。)

药师: Please take a seat. The doctor will see you soon. (请先坐，医生很快会看您。)

老爷爷: Do I need any tests? (需要做什么检查吗？)

药师: A blood test would help. The lab is downstairs. (建议抽血，化验在楼下。)

老爷爷: How should I take this medicine? (这个药怎么吃？)

药师: Twice a day after meals. (一天两次，饭后服用。)

老爷爷: Where do I pay? (在哪里缴费？)

药师: At window number three, then return here. (三号窗口缴费，再回来。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "就医问诊", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "社区药房问药，日常健康英语。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "就医问诊", "sceneSub": "医院药房", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'012cf6e38d1636f8cd23171bca314219','老奶奶·图书馆借书英语📚','生成要求：9:16竖屏，15-20秒。中国校园图书馆借还台。语速明快。风格：青春学习风。

画面描述：老奶奶（约60-72岁，银发微卷，浅色羽绒服与围巾，温暖）正在「校园生活/图书馆借书」场景中，在「图书馆借书」用英语完成沟通，主角是老奶奶。配角为管理员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；管理员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, is this the right place for borrowing books? (请问这里是borrowing books吗？)

管理员: Yes. What do you need? (是的，你需要什么？)

老奶奶: I''d like to borrowing books. (我想borrowing books。)

管理员: Sure. Please fill in this form. (好的，请填这张表。)

老奶奶: What time does it close today? (今天几点关门？)

管理员: At nine in the evening. (晚上九点。)

老奶奶: Do I need my student card? (需要学生卡吗？)

管理员: Yes, please tap it here. (需要，请在这里刷一下。)

老奶奶: Thank you for your help. (谢谢你帮忙。)

管理员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "图书馆借还书刷卡，校园刚需口语。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "图书馆借书", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b8790a1e8b0d8c0a93d53cd0b49c2d11','图书馆借书英语跟读📚','生成要求：9:16竖屏，15-20秒。中国校园图书馆借还台。语速明快。风格：青春学习风。

画面描述：少年（约15-18岁，棒球服外套，耳机挂颈，轻松）正在「校园生活/图书馆借书」场景中，在「图书馆借书」用英语完成沟通，主角是少年。配角为管理员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；管理员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, is this the right place for borrowing books? (请问这里是borrowing books吗？)

管理员: Yes. What do you need? (是的，你需要什么？)

少年: I''d like to borrowing books. (我想borrowing books。)

管理员: Sure. Please fill in this form. (好的，请填这张表。)

少年: What time does it close today? (今天几点关门？)

管理员: At nine in the evening. (晚上九点。)

少年: Do I need my student card? (需要学生卡吗？)

管理员: Yes, please tap it here. (需要，请在这里刷一下。)

少年: Thank you for your help. (谢谢你帮忙。)

管理员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "图书馆借还书刷卡，校园刚需口语。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "图书馆借书", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'51c3eedc8ef20ad115528ddfd65deb69','英语·教室找座位现场📚','生成要求：9:16竖屏，15-20秒。中国大学阶梯教室。语速明快。风格：校园日常风。

画面描述：老爷爷（约63-75岁，白眉短发，唐装上衣与布鞋，从容）正在「校园生活/教室找座位」场景中，在「教室找座位」用英语完成沟通，主角是老爷爷。配角为同学。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；同学声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, is this the right place for finding a seat? (请问这里是finding a seat吗？)

同学: Yes. What do you need? (是的，你需要什么？)

老爷爷: I''d like to finding a seat. (我想finding a seat。)

同学: Sure. Please fill in this form. (好的，请填这张表。)

老爷爷: What time does it close today? (今天几点关门？)

同学: At nine in the evening. (晚上九点。)

老爷爷: Do I need my student card? (需要学生卡吗？)

同学: Yes, please tap it here. (需要，请在这里刷一下。)

老爷爷: Thank you for your help. (谢谢你帮忙。)

同学: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "阶梯教室找座位，跟同学自然搭话。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "教室找座位", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'5835219b8af89ea0ff6ef902ad68c7e3','教室找座位开口说英语📚','生成要求：9:16竖屏，15-20秒。中国大学阶梯教室。语速明快。风格：校园日常风。

画面描述：年轻女性（约21-27岁，短发利落，条纹衬衫与托特包，清爽）正在「校园生活/教室找座位」场景中，在「教室找座位」用英语完成沟通，主角是年轻女性。配角为同学。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；同学声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, is this the right place for finding a seat? (请问这里是finding a seat吗？)

同学: Yes. What do you need? (是的，你需要什么？)

年轻女性: I''d like to finding a seat. (我想finding a seat。)

同学: Sure. Please fill in this form. (好的，请填这张表。)

年轻女性: What time does it close today? (今天几点关门？)

同学: At nine in the evening. (晚上九点。)

年轻女性: Do I need my student card? (需要学生卡吗？)

同学: Yes, please tap it here. (需要，请在这里刷一下。)

年轻女性: Thank you for your help. (谢谢你帮忙。)

同学: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "阶梯教室找座位，跟同学自然搭话。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "教室找座位", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'81cc58023570545ab71cdea80fe553d2','校园食堂开口说英语📚','生成要求：9:16竖屏，15-20秒。中国校园食堂窗口。语速明快。风格：青春烟火风。

画面描述：小女孩（约8-10岁，戴棒球帽，卡通印花短袖，背迷你双肩包）正在「校园生活/校园食堂」场景中，在「校园食堂」用英语完成沟通，主角是小女孩。配角为打菜阿姨。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；打菜阿姨声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, is this the right place for the campus canteen? (请问这里是the campus canteen吗？)

打菜阿姨: Yes. What do you need? (是的，你需要什么？)

小女孩: I''d like to the campus canteen. (我想the campus canteen。)

打菜阿姨: Sure. Please fill in this form. (好的，请填这张表。)

小女孩: What time does it close today? (今天几点关门？)

打菜阿姨: At nine in the evening. (晚上九点。)

小女孩: Do I need my student card? (需要学生卡吗？)

打菜阿姨: Yes, please tap it here. (需要，请在这里刷一下。)

小女孩: Thank you for your help. (谢谢你帮忙。)

打菜阿姨: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "食堂窗口打菜，学生党天天能练。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园食堂", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'94cf52e6f6c0725b44ade1b6c11e32bf','年轻男性版校园食堂英语📚','生成要求：9:16竖屏，15-20秒。中国校园食堂窗口。语速明快。风格：青春烟火风。

画面描述：年轻男性（约23-29岁，黑框眼镜，针织开衫与卡其裤，温和）正在「校园生活/校园食堂」场景中，在「校园食堂」用英语完成沟通，主角是年轻男性。配角为打菜阿姨。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；打菜阿姨声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, is this the right place for the campus canteen? (请问这里是the campus canteen吗？)

打菜阿姨: Yes. What do you need? (是的，你需要什么？)

年轻男性: I''d like to the campus canteen. (我想the campus canteen。)

打菜阿姨: Sure. Please fill in this form. (好的，请填这张表。)

年轻男性: What time does it close today? (今天几点关门？)

打菜阿姨: At nine in the evening. (晚上九点。)

年轻男性: Do I need my student card? (需要学生卡吗？)

打菜阿姨: Yes, please tap it here. (需要，请在这里刷一下。)

年轻男性: Thank you for your help. (谢谢你帮忙。)

打菜阿姨: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "食堂窗口打菜，学生党天天能练。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园食堂", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ecf7f95460a6911e3207e13e98113c7b','跟读|宿舍门禁英语📚','生成要求：9:16竖屏，15-20秒。中国学生宿舍楼门禁。语速明快。风格：校园生活风。

画面描述：小男孩（约6-9岁，侧分短发，格子衬衫配牛仔裤，乖巧）正在「校园生活/宿舍门禁」场景中，在「宿舍门禁」用英语完成沟通，主角是小男孩。配角为宿管。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；宿管声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, is this the right place for dorm access? (请问这里是dorm access吗？)

宿管: Yes. What do you need? (是的，你需要什么？)

小男孩: I''d like to dorm access. (我想dorm access。)

宿管: Sure. Please fill in this form. (好的，请填这张表。)

小男孩: What time does it close today? (今天几点关门？)

宿管: At nine in the evening. (晚上九点。)

小男孩: Do I need my student card? (需要学生卡吗？)

宿管: Yes, please tap it here. (需要，请在这里刷一下。)

小男孩: Thank you for your help. (谢谢你帮忙。)

宿管: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "宿舍门禁刷卡对话，宿管场景真实。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "宿舍门禁", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6cfbbda94dfa66013f466930884fd6d7','宿舍门禁情景英语📚','生成要求：9:16竖屏，15-20秒。中国学生宿舍楼门禁。语速明快。风格：校园生活风。

画面描述：中年女性（约36-46岁，黑框眼镜，棉质外套与帆布包，亲切）正在「校园生活/宿舍门禁」场景中，在「宿舍门禁」用英语完成沟通，主角是中年女性。配角为宿管。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；宿管声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, is this the right place for dorm access? (请问这里是dorm access吗？)

宿管: Yes. What do you need? (是的，你需要什么？)

中年女性: I''d like to dorm access. (我想dorm access。)

宿管: Sure. Please fill in this form. (好的，请填这张表。)

中年女性: What time does it close today? (今天几点关门？)

宿管: At nine in the evening. (晚上九点。)

中年女性: Do I need my student card? (需要学生卡吗？)

宿管: Yes, please tap it here. (需要，请在这里刷一下。)

中年女性: Thank you for your help. (谢谢你帮忙。)

宿管: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "宿舍门禁刷卡对话，宿管场景真实。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "宿舍门禁", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'5a2f49b202357eff2a7e72a2f206b59b','生活英语·老师办公室📚','生成要求：9:16竖屏，15-20秒。中国学校教师办公室门口。语速明快。风格：礼貌校园风。

画面描述：少女（约15-18岁，空气刘海，格子衬衫叠穿T恤，元气）正在「校园生活/老师办公室」场景中，在「老师办公室」用英语完成沟通，主角是少女。配角为老师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；老师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, is this the right place for the teacher''s office? (请问这里是the teacher''s office吗？)

老师: Yes. What do you need? (是的，你需要什么？)

少女: I''d like to the teacher''s office. (我想the teacher''s office。)

老师: Sure. Please fill in this form. (好的，请填这张表。)

少女: What time does it close today? (今天几点关门？)

老师: At nine in the evening. (晚上九点。)

少女: Do I need my student card? (需要学生卡吗？)

老师: Yes, please tap it here. (需要，请在这里刷一下。)

少女: Thank you for your help. (谢谢你帮忙。)

老师: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "找老师办公室怎么说，礼貌又大方。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "老师办公室", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'01960897de4389e8612453e43a73d27b','中年男性的老师办公室英语📚','生成要求：9:16竖屏，15-20秒。中国学校教师办公室门口。语速明快。风格：礼貌校园风。

画面描述：中年男性（约38-50岁，微秃短发，冲锋衣与背包，务实）正在「校园生活/老师办公室」场景中，在「老师办公室」用英语完成沟通，主角是中年男性。配角为老师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；老师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, is this the right place for the teacher''s office? (请问这里是the teacher''s office吗？)

老师: Yes. What do you need? (是的，你需要什么？)

中年男性: I''d like to the teacher''s office. (我想the teacher''s office。)

老师: Sure. Please fill in this form. (好的，请填这张表。)

中年男性: What time does it close today? (今天几点关门？)

老师: At nine in the evening. (晚上九点。)

中年男性: Do I need my student card? (需要学生卡吗？)

老师: Yes, please tap it here. (需要，请在这里刷一下。)

中年男性: Thank you for your help. (谢谢你帮忙。)

老师: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "找老师办公室怎么说，礼貌又大方。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "老师办公室", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'0e83a015d7b48f1906a178c0dd394a53','少年·实验室安全英语📚','生成要求：9:16竖屏，15-20秒。中国大学实验室门口。语速明快。风格：严谨校园风。

画面描述：少年（约17-18岁，黑框眼镜，针织毛衣与长裤，书卷气）正在「校园生活/实验室安全」场景中，在「实验室安全」用英语完成沟通，主角是少年。配角为实验员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；实验员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Excuse me, is this the right place for lab safety rules? (请问这里是lab safety rules吗？)

实验员: Yes. What do you need? (是的，你需要什么？)

少年: I''d like to lab safety rules. (我想lab safety rules。)

实验员: Sure. Please fill in this form. (好的，请填这张表。)

少年: What time does it close today? (今天几点关门？)

实验员: At nine in the evening. (晚上九点。)

少年: Do I need my student card? (需要学生卡吗？)

实验员: Yes, please tap it here. (需要，请在这里刷一下。)

少年: Thank you for your help. (谢谢你帮忙。)

实验员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "进实验室先问安全须知，专业又清楚。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "实验室安全", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'7576d19f8b5e96ad1d6bd0504443efb4','实验室安全英语跟读📚','生成要求：9:16竖屏，15-20秒。中国大学实验室门口。语速明快。风格：严谨校园风。

画面描述：老奶奶（约63-75岁，短发利落，唐装式盘扣外套，精神）正在「校园生活/实验室安全」场景中，在「实验室安全」用英语完成沟通，主角是老奶奶。配角为实验员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；实验员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Excuse me, is this the right place for lab safety rules? (请问这里是lab safety rules吗？)

实验员: Yes. What do you need? (是的，你需要什么？)

老奶奶: I''d like to lab safety rules. (我想lab safety rules。)

实验员: Sure. Please fill in this form. (好的，请填这张表。)

老奶奶: What time does it close today? (今天几点关门？)

实验员: At nine in the evening. (晚上九点。)

老奶奶: Do I need my student card? (需要学生卡吗？)

实验员: Yes, please tap it here. (需要，请在这里刷一下。)

老奶奶: Thank you for your help. (谢谢你帮忙。)

实验员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "进实验室先问安全须知，专业又清楚。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "实验室安全", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9e331c5097f17cfbae894df3151ec329','英语·体育馆办卡现场📚','生成要求：9:16竖屏，15-20秒。中国校园体育馆前台。语速明快。风格：运动青春风。

画面描述：年轻女性（约23-29岁，微卷发，碎花连衣裙与凉鞋，温柔）正在「校园生活/体育馆办卡」场景中，在「体育馆办卡」用英语完成沟通，主角是年轻女性。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Excuse me, is this the right place for getting a gym card? (请问这里是getting a gym card吗？)

前台: Yes. What do you need? (是的，你需要什么？)

年轻女性: I''d like to getting a gym card. (我想getting a gym card。)

前台: Sure. Please fill in this form. (好的，请填这张表。)

年轻女性: What time does it close today? (今天几点关门？)

前台: At nine in the evening. (晚上九点。)

年轻女性: Do I need my student card? (需要学生卡吗？)

前台: Yes, please tap it here. (需要，请在这里刷一下。)

年轻女性: Thank you for your help. (谢谢你帮忙。)

前台: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "体育馆办卡问时间，运动也能练英语。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "体育馆办卡", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c849e23f87ecfd0494b5e6b1a3ef28d2','体育馆办卡开口说英语📚','生成要求：9:16竖屏，15-20秒。中国校园体育馆前台。语速明快。风格：运动青春风。

画面描述：老爷爷（约60-72岁，微秃白发，冲锋衣，可选手持登山杖，户外感）正在「校园生活/体育馆办卡」场景中，在「体育馆办卡」用英语完成沟通，主角是老爷爷。配角为前台。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；前台声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Excuse me, is this the right place for getting a gym card? (请问这里是getting a gym card吗？)

前台: Yes. What do you need? (是的，你需要什么？)

老爷爷: I''d like to getting a gym card. (我想getting a gym card。)

前台: Sure. Please fill in this form. (好的，请填这张表。)

老爷爷: What time does it close today? (今天几点关门？)

前台: At nine in the evening. (晚上九点。)

老爷爷: Do I need my student card? (需要学生卡吗？)

前台: Yes, please tap it here. (需要，请在这里刷一下。)

老爷爷: Thank you for your help. (谢谢你帮忙。)

前台: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "体育馆办卡问时间，运动也能练英语。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "体育馆办卡", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1029c89ecd599ba4e685606b62ac55f6','报名窗口开口说英语📚','生成要求：9:16竖屏，15-20秒。中国学校教务报名窗口。语速明快。风格：实用校园风。

画面描述：年轻男性（约21-27岁，短刺头，牛仔夹克配T恤，随性）正在「校园生活/报名窗口」场景中，在「报名窗口」用英语完成沟通，主角是年轻男性。配角为老师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；老师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Excuse me, is this the right place for course registration? (请问这里是course registration吗？)

老师: Yes. What do you need? (是的，你需要什么？)

年轻男性: I''d like to course registration. (我想course registration。)

老师: Sure. Please fill in this form. (好的，请填这张表。)

年轻男性: What time does it close today? (今天几点关门？)

老师: At nine in the evening. (晚上九点。)

年轻男性: Do I need my student card? (需要学生卡吗？)

老师: Yes, please tap it here. (需要，请在这里刷一下。)

年轻男性: Thank you for your help. (谢谢你帮忙。)

老师: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "教务报名窗口沟通，别只会递材料。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "报名窗口", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'0d14b49f8828aad7e529941e3e54b5fb','小女孩版报名窗口英语📚','生成要求：9:16竖屏，15-20秒。中国学校教务报名窗口。语速明快。风格：实用校园风。

画面描述：小女孩（约6-10岁，羊角辫，碎花短袖裙，手拿折叠扇）正在「校园生活/报名窗口」场景中，在「报名窗口」用英语完成沟通，主角是小女孩。配角为老师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；老师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Excuse me, is this the right place for course registration? (请问这里是course registration吗？)

老师: Yes. What do you need? (是的，你需要什么？)

小女孩: I''d like to course registration. (我想course registration。)

老师: Sure. Please fill in this form. (好的，请填这张表。)

小女孩: What time does it close today? (今天几点关门？)

老师: At nine in the evening. (晚上九点。)

小女孩: Do I need my student card? (需要学生卡吗？)

老师: Yes, please tap it here. (需要，请在这里刷一下。)

小女孩: Thank you for your help. (谢谢你帮忙。)

老师: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "教务报名窗口沟通，别只会递材料。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "报名窗口", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c3c3cb1895f2047f6c5227094b3669af','跟读|校园打印店英语📚','生成要求：9:16竖屏，15-20秒。中国校园打印店柜台。语速明快。风格：学生日常风。

画面描述：中年女性（约38-50岁，染浅棕发，条纹衬衫卷袖，日常家居外出发型）正在「校园生活/校园打印店」场景中，在「校园打印店」用英语完成沟通，主角是中年女性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Excuse me, is this the right place for printing handouts? (请问这里是printing handouts吗？)

店员: Yes. What do you need? (是的，你需要什么？)

中年女性: I''d like to printing handouts. (我想printing handouts。)

店员: Sure. Please fill in this form. (好的，请填这张表。)

中年女性: What time does it close today? (今天几点关门？)

店员: At nine in the evening. (晚上九点。)

中年女性: Do I need my student card? (需要学生卡吗？)

店员: Yes, please tap it here. (需要，请在这里刷一下。)

中年女性: Thank you for your help. (谢谢你帮忙。)

店员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "打印店改格式交文件，超高频。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园打印店", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'794224d9faa5fa2b93a199af3f8eec98','校园打印店情景英语📚','生成要求：9:16竖屏，15-20秒。中国校园打印店柜台。语速明快。风格：学生日常风。

画面描述：小男孩（约7-10岁，棒球帽反戴，印花短袖，滑板鞋）正在「校园生活/校园打印店」场景中，在「校园打印店」用英语完成沟通，主角是小男孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Excuse me, is this the right place for printing handouts? (请问这里是printing handouts吗？)

店员: Yes. What do you need? (是的，你需要什么？)

小男孩: I''d like to printing handouts. (我想printing handouts。)

店员: Sure. Please fill in this form. (好的，请填这张表。)

小男孩: What time does it close today? (今天几点关门？)

店员: At nine in the evening. (晚上九点。)

小男孩: Do I need my student card? (需要学生卡吗？)

店员: Yes, please tap it here. (需要，请在这里刷一下。)

小男孩: Thank you for your help. (谢谢你帮忙。)

店员: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "打印店改格式交文件，超高频。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园打印店", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'782a15c682208166e0937de8a0c92eee','生活英语·校园班车📚','生成要求：9:16竖屏，15-20秒。中国校园班车站点。语速明快。风格：通勤校园风。

画面描述：中年男性（约36-46岁，干净短发，白衬衫卷袖与腕表，干练）正在「校园生活/校园班车」场景中，在「校园班车」用英语完成沟通，主角是中年男性。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Excuse me, is this the right place for the campus shuttle? (请问这里是the campus shuttle吗？)

司机: Yes. What do you need? (是的，你需要什么？)

中年男性: I''d like to the campus shuttle. (我想the campus shuttle。)

司机: Sure. Please fill in this form. (好的，请填这张表。)

中年男性: What time does it close today? (今天几点关门？)

司机: At nine in the evening. (晚上九点。)

中年男性: Do I need my student card? (需要学生卡吗？)

司机: Yes, please tap it here. (需要，请在这里刷一下。)

中年男性: Thank you for your help. (谢谢你帮忙。)

司机: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "校园班车站确认方向，短对话高复用。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园班车", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'07c7855aae99189ab3cce27d839c3560','少女的校园班车英语📚','生成要求：9:16竖屏，15-20秒。中国校园班车站点。语速明快。风格：通勤校园风。

画面描述：少女（约17-18岁，高马尾，运动外套与短裤，刚下体育课感）正在「校园生活/校园班车」场景中，在「校园班车」用英语完成沟通，主角是少女。配角为司机。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；司机声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Excuse me, is this the right place for the campus shuttle? (请问这里是the campus shuttle吗？)

司机: Yes. What do you need? (是的，你需要什么？)

少女: I''d like to the campus shuttle. (我想the campus shuttle。)

司机: Sure. Please fill in this form. (好的，请填这张表。)

少女: What time does it close today? (今天几点关门？)

司机: At nine in the evening. (晚上九点。)

少女: Do I need my student card? (需要学生卡吗？)

司机: Yes, please tap it here. (需要，请在这里刷一下。)

少女: Thank you for your help. (谢谢你帮忙。)

司机: No problem. See you around campus! (没事，校园见！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "校园生活", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "校园班车站确认方向，短对话高复用。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "校园生活", "sceneSub": "校园班车", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'21abbdfe5be1a639c1952d6f5f37a012','小男孩·银行开户英语🏦','生成要求：9:16竖屏，15-20秒。中国银行网点开户柜台。语速明快。风格：正式实用风。

画面描述：小男孩（约6-10岁，平头，深蓝Polo与卡其短裤，认真听讲）正在「银行邮政/银行开户」场景中，在「银行开户」用英语完成沟通，主角是小男孩。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I''d like to open an account. (你好，我想open an account。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

小男孩: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

小男孩: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

小男孩: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

小男孩: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "银行开户带什么证件，柜员对话一次懂。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "银行开户", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'48538d877ae16498d58737ffb1c1bad4','银行开户英语跟读🏦','生成要求：9:16竖屏，15-20秒。中国银行网点开户柜台。语速明快。风格：正式实用风。

画面描述：中年女性（约35-48岁，马尾，运动外套与休闲裤，利落）正在「银行邮政/银行开户」场景中，在「银行开户」用英语完成沟通，主角是中年女性。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I''d like to open an account. (你好，我想open an account。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

中年女性: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

中年女性: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

中年女性: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

中年女性: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "银行开户带什么证件，柜员对话一次懂。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "银行开户", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'91a6282d607bfe2dd506c677fb2d34ab','英语·柜台取款现场🏦','生成要求：9:16竖屏，15-20秒。中国银行柜台取款窗口。语速明快。风格：金融实用风。

画面描述：少女（约15-17岁，侧分长发，米色风衣与帆布袋，沉稳）正在「银行邮政/柜台取款」场景中，在「柜台取款」用英语完成沟通，主角是少女。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I''d like to withdraw cash. (你好，我想withdraw cash。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

少女: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

少女: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

少女: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

少女: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "柜台取款怎么说金额，清楚不尴尬。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "柜台取款", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'cd25ba02adb8341361a92e718424f5c4','柜台取款开口说英语🏦','生成要求：9:16竖屏，15-20秒。中国银行柜台取款窗口。语速明快。风格：金融实用风。

画面描述：中年男性（约40-50岁，花白鬓角，羊毛衫与长裤，慈和）正在「银行邮政/柜台取款」场景中，在「柜台取款」用英语完成沟通，主角是中年男性。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I''d like to withdraw cash. (你好，我想withdraw cash。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

中年男性: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

中年男性: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

中年男性: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

中年男性: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "柜台取款怎么说金额，清楚不尴尬。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "柜台取款", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d8fee8f984125068b52c7ee47adeec6f','汇款转账开口说英语🏦','生成要求：9:16竖屏，15-20秒。中国银行转账业务台。语速明快。风格：稳重实用风。

画面描述：少年（约15-17岁，短碎发，球队球衣与运动裤，活力）正在「银行邮政/汇款转账」场景中，在「汇款转账」用英语完成沟通，主角是少年。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I''d like to make a transfer. (你好，我想make a transfer。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

少年: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

少年: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

少年: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

少年: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "转账汇款确认到账，办事英语刚需。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "汇款转账", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'859d04ffb4649f18613a4b8e1a9b90f3','老奶奶版汇款转账英语🏦','生成要求：9:16竖屏，15-20秒。中国银行转账业务台。语速明快。风格：稳重实用风。

画面描述：老奶奶（约60-70岁，花白马尾松松扎起，围裙外披外套，烟火气）正在「银行邮政/汇款转账」场景中，在「汇款转账」用英语完成沟通，主角是老奶奶。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I''d like to make a transfer. (你好，我想make a transfer。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

老奶奶: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

老奶奶: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

老奶奶: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

老奶奶: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "转账汇款确认到账，办事英语刚需。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "汇款转账", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e01bc3e8818beec1c6fcb87fa52ccc1b','跟读|外币兑换英语🏦','生成要求：9:16竖屏，15-20秒。中国银行外币兑换窗口。语速明快。风格：旅行金融风。

画面描述：年轻女性（约20-28岁，黑长直，羽绒服内搭卫衣，日常街拍感）正在「银行邮政/外币兑换」场景中，在「外币兑换」用英语完成沟通，主角是年轻女性。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I''d like to exchange currency. (你好，我想exchange currency。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

年轻女性: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

年轻女性: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

年轻女性: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

年轻女性: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外币兑换窗口问汇率，旅游前必会。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "外币兑换", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1e6370d508c5b843225a4d81d5258ec7','外币兑换情景英语🏦','生成要求：9:16竖屏，15-20秒。中国银行外币兑换窗口。语速明快。风格：旅行金融风。

画面描述：老爷爷（约65-80岁，花白背头，呢大衣与围巾，庄重）正在「银行邮政/外币兑换」场景中，在「外币兑换」用英语完成沟通，主角是老爷爷。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I''d like to exchange currency. (你好，我想exchange currency。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

老爷爷: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

老爷爷: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

老爷爷: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

老爷爷: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "外币兑换窗口问汇率，旅游前必会。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "外币兑换", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'68a98679e2739aea9d977e59faf49946','生活英语·信用卡办理🏦','生成要求：9:16竖屏，15-20秒。中国银行信用卡办理台。语速明快。风格：都市金融风。

画面描述：年轻男性（约25-30岁，干净短发，深色Polo与休闲裤，稳重）正在「银行邮政/信用卡办理」场景中，在「信用卡办理」用英语完成沟通，主角是年轻男性。配角为客户经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；客户经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I''d like to apply for a credit card. (你好，我想apply for a credit card。)

客户经理: Please take a number and wait a moment. (请先取号稍等。)

年轻男性: Do I need my passport or ID card? (需要护照还是身份证？)

客户经理: Your ID card is enough. (身份证就可以。)

年轻男性: How long will this take? (大概要多久？)

客户经理: About fifteen minutes. (大概十五分钟。)

年轻男性: Is there a service fee? (有手续费吗？)

客户经理: A small fee, five yuan. (有一点，五元。)

年轻男性: Could you confirm it''s done? (能确认已经办好了吗？)

客户经理: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "办信用卡问额度费用，客户经理对谈。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "信用卡办理", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4f06a35c338fe7055b92b34ee1f7db83','小女孩的信用卡办理英语🏦','生成要求：9:16竖屏，15-20秒。中国银行信用卡办理台。语速明快。风格：都市金融风。

画面描述：小女孩（约7-10岁，短发刘海，素色Polo衫，干净清爽）正在「银行邮政/信用卡办理」场景中，在「信用卡办理」用英语完成沟通，主角是小女孩。配角为客户经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；客户经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I''d like to apply for a credit card. (你好，我想apply for a credit card。)

客户经理: Please take a number and wait a moment. (请先取号稍等。)

小女孩: Do I need my passport or ID card? (需要护照还是身份证？)

客户经理: Your ID card is enough. (身份证就可以。)

小女孩: How long will this take? (大概要多久？)

客户经理: About fifteen minutes. (大概十五分钟。)

小女孩: Is there a service fee? (有手续费吗？)

客户经理: A small fee, five yuan. (有一点，五元。)

小女孩: Could you confirm it''s done? (能确认已经办好了吗？)

客户经理: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "办信用卡问额度费用，客户经理对谈。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "信用卡办理", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'519dfea1d8b37d37e509d0419b024b01','中年女性·邮局寄件英语🏦','生成要求：9:16竖屏，15-20秒。中国邮局寄件柜台。语速明快。风格：生活服务风。

画面描述：中年女性（约40-50岁，齐耳短发，深色羽绒服与围巾，温暖）正在「银行邮政/邮局寄件」场景中，在「邮局寄件」用英语完成沟通，主角是中年女性。配角为营业员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；营业员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hello, I''d like to mail a package. (你好，我想mail a package。)

营业员: Please take a number and wait a moment. (请先取号稍等。)

中年女性: Do I need my passport or ID card? (需要护照还是身份证？)

营业员: Your ID card is enough. (身份证就可以。)

中年女性: How long will this take? (大概要多久？)

营业员: About fifteen minutes. (大概十五分钟。)

中年女性: Is there a service fee? (有手续费吗？)

营业员: A small fee, five yuan. (有一点，五元。)

中年女性: Could you confirm it''s done? (能确认已经办好了吗？)

营业员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "邮局寄件称重贴单，一步步跟读。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "邮局寄件", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a78bac42eacfbe1d1214c6c7f9846a83','邮局寄件英语跟读🏦','生成要求：9:16竖屏，15-20秒。中国邮局寄件柜台。语速明快。风格：生活服务风。

画面描述：小男孩（约8-9岁，轻微卷发，卫衣帽子半戴，轻松自然）正在「银行邮政/邮局寄件」场景中，在「邮局寄件」用英语完成沟通，主角是小男孩。配角为营业员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；营业员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hello, I''d like to mail a package. (你好，我想mail a package。)

营业员: Please take a number and wait a moment. (请先取号稍等。)

小男孩: Do I need my passport or ID card? (需要护照还是身份证？)

营业员: Your ID card is enough. (身份证就可以。)

小男孩: How long will this take? (大概要多久？)

营业员: About fifteen minutes. (大概十五分钟。)

小男孩: Is there a service fee? (有手续费吗？)

营业员: A small fee, five yuan. (有一点，五元。)

小男孩: Could you confirm it''s done? (能确认已经办好了吗？)

营业员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "邮局寄件称重贴单，一步步跟读。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "邮局寄件", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e522eb4995043494ca5e37ff2f1b69f7','英语·快递取件现场🏦','生成要求：9:16竖屏，15-20秒。中国小区快递驿站。语速明快。风格：邻里生活风。

画面描述：中年男性（约35-48岁，短刺头，牛仔外套与工装裤，接地气）正在「银行邮政/快递取件」场景中，在「快递取件」用英语完成沟通，主角是中年男性。配角为驿站店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；驿站店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hello, I''d like to pick up a parcel. (你好，我想pick up a parcel。)

驿站店员: Please take a number and wait a moment. (请先取号稍等。)

中年男性: Do I need my passport or ID card? (需要护照还是身份证？)

驿站店员: Your ID card is enough. (身份证就可以。)

中年男性: How long will this take? (大概要多久？)

驿站店员: About fifteen minutes. (大概十五分钟。)

中年男性: Is there a service fee? (有手续费吗？)

驿站店员: A small fee, five yuan. (有一点，五元。)

中年男性: Could you confirm it''s done? (能确认已经办好了吗？)

驿站店员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "驿站取件报取件码，小区天天能用。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "快递取件", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e77c4778d6841ce1fa07034ec8663383','快递取件开口说英语🏦','生成要求：9:16竖屏，15-20秒。中国小区快递驿站。语速明快。风格：邻里生活风。

画面描述：少女（约16-18岁，双马尾，学院风百褶裙，笑意温柔）正在「银行邮政/快递取件」场景中，在「快递取件」用英语完成沟通，主角是少女。配角为驿站店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；驿站店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hello, I''d like to pick up a parcel. (你好，我想pick up a parcel。)

驿站店员: Please take a number and wait a moment. (请先取号稍等。)

少女: Do I need my passport or ID card? (需要护照还是身份证？)

驿站店员: Your ID card is enough. (身份证就可以。)

少女: How long will this take? (大概要多久？)

驿站店员: About fifteen minutes. (大概十五分钟。)

少女: Is there a service fee? (有手续费吗？)

驿站店员: A small fee, five yuan. (有一点，五元。)

少女: Could you confirm it''s done? (能确认已经办好了吗？)

驿站店员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "驿站取件报取件码，小区天天能用。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "快递取件", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'1bf176f9ee9fbc035dccd78f1d7e300b','生活缴费开口说英语🏦','生成要求：9:16竖屏，15-20秒。中国银行生活缴费机旁。语速明快。风格：日常实用风。

画面描述：老奶奶（约65-80岁，白发梳齐，深色毛呢大衣，慢条斯理）正在「银行邮政/生活缴费」场景中，在「生活缴费」用英语完成沟通，主角是老奶奶。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hello, I''d like to pay utility bills. (你好，我想pay utility bills。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

老奶奶: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

老奶奶: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

老奶奶: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

老奶奶: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "生活缴费机旁求助，实用到哭。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "生活缴费", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'21b5d99be50258c1dde9b8dec6f67b1a','少年版生活缴费英语🏦','生成要求：9:16竖屏，15-20秒。中国银行生活缴费机旁。语速明快。风格：日常实用风。

画面描述：少年（约16-18岁，干净短发，浅色衬衫卷袖，礼貌微笑）正在「银行邮政/生活缴费」场景中，在「生活缴费」用英语完成沟通，主角是少年。配角为柜员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；柜员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hello, I''d like to pay utility bills. (你好，我想pay utility bills。)

柜员: Please take a number and wait a moment. (请先取号稍等。)

少年: Do I need my passport or ID card? (需要护照还是身份证？)

柜员: Your ID card is enough. (身份证就可以。)

少年: How long will this take? (大概要多久？)

柜员: About fifteen minutes. (大概十五分钟。)

少年: Is there a service fee? (有手续费吗？)

柜员: A small fee, five yuan. (有一点，五元。)

少年: Could you confirm it''s done? (能确认已经办好了吗？)

柜员: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "生活缴费机旁求助，实用到哭。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "生活缴费", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'2840a18578a0021bb8b384934b26afa6','跟读|理财咨询英语🏦','生成要求：9:16竖屏，15-20秒。中国银行理财室门口。语速明快。风格：稳重沟通风。

画面描述：老爷爷（约62-70岁，银发平头，格子衬衫卷袖，笑眯眯）正在「银行邮政/理财咨询」场景中，在「理财咨询」用英语完成沟通，主角是老爷爷。配角为理财经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；理财经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hello, I''d like to ask about savings. (你好，我想ask about savings。)

理财经理: Please take a number and wait a moment. (请先取号稍等。)

老爷爷: Do I need my passport or ID card? (需要护照还是身份证？)

理财经理: Your ID card is enough. (身份证就可以。)

老爷爷: How long will this take? (大概要多久？)

理财经理: About fifteen minutes. (大概十五分钟。)

老爷爷: Is there a service fee? (有手续费吗？)

理财经理: A small fee, five yuan. (有一点，五元。)

老爷爷: Could you confirm it''s done? (能确认已经办好了吗？)

理财经理: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "理财室门口预约咨询，开口更稳。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "理财咨询", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9554823328e0743c37c53df029085b0f','理财咨询情景英语🏦','生成要求：9:16竖屏，15-20秒。中国银行理财室门口。语速明快。风格：稳重沟通风。

画面描述：年轻女性（约22-30岁，侧分长发，西装外套配阔腿裤，利落）正在「银行邮政/理财咨询」场景中，在「理财咨询」用英语完成沟通，主角是年轻女性。配角为理财经理。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；理财经理声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hello, I''d like to ask about savings. (你好，我想ask about savings。)

理财经理: Please take a number and wait a moment. (请先取号稍等。)

年轻女性: Do I need my passport or ID card? (需要护照还是身份证？)

理财经理: Your ID card is enough. (身份证就可以。)

年轻女性: How long will this take? (大概要多久？)

理财经理: About fifteen minutes. (大概十五分钟。)

年轻女性: Is there a service fee? (有手续费吗？)

理财经理: A small fee, five yuan. (有一点，五元。)

年轻女性: Could you confirm it''s done? (能确认已经办好了吗？)

理财经理: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "理财室门口预约咨询，开口更稳。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "理财咨询", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b673a054e91e58ba632c6049c9608442','生活英语·ATM协助💳','生成要求：9:16竖屏，15-20秒。中国银行ATM机旁。语速明快。风格：街头实用风。

画面描述：小女孩（约6-9岁，侧边编发，牛仔短裤配白T恤，俏皮）正在「银行邮政/ATM协助」场景中，在「ATM协助」用英语完成沟通，主角是小女孩。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hello, I''d like to use the ATM. (你好，我想use the ATM。)

保安: Please take a number and wait a moment. (请先取号稍等。)

小女孩: Do I need my passport or ID card? (需要护照还是身份证？)

保安: Your ID card is enough. (身份证就可以。)

小女孩: How long will this take? (大概要多久？)

保安: About fifteen minutes. (大概十五分钟。)

小女孩: Is there a service fee? (有手续费吗？)

保安: A small fee, five yuan. (有一点，五元。)

小女孩: Could you confirm it''s done? (能确认已经办好了吗？)

保安: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "ATM旁边求助，保安也能接住你。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "ATM协助", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a50ea565d8524d4e28e5edca58abdb05','年轻男性的ATM协助英语💳','生成要求：9:16竖屏，15-20秒。中国银行ATM机旁。语速明快。风格：街头实用风。

画面描述：年轻男性（约20-28岁，棒球帽，冲锋衣与运动鞋，户外感）正在「银行邮政/ATM协助」场景中，在「ATM协助」用英语完成沟通，主角是年轻男性。配角为保安。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；保安声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hello, I''d like to use the ATM. (你好，我想use the ATM。)

保安: Please take a number and wait a moment. (请先取号稍等。)

年轻男性: Do I need my passport or ID card? (需要护照还是身份证？)

保安: Your ID card is enough. (身份证就可以。)

年轻男性: How long will this take? (大概要多久？)

保安: About fifteen minutes. (大概十五分钟。)

年轻男性: Is there a service fee? (有手续费吗？)

保安: A small fee, five yuan. (有一点，五元。)

年轻男性: Could you confirm it''s done? (能确认已经办好了吗？)

保安: Yes. Here''s your receipt. Have a nice day! (办好了，这是回单。祝您愉快！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "银行邮政", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "ATM旁边求助，保安也能接住你。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "银行邮政", "sceneSub": "ATM协助", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'34a96a968642f2a634ee0ac31fddf6f0','年轻女性·理发店英语🔑','生成要求：9:16竖屏，15-20秒。中国街边理发店座椅旁。语速明快。风格：生活服务风。

画面描述：年轻女性（约20-30岁，披肩长发，米色风衣与乐福鞋，自信自然）正在「日常服务/理发店」场景中，在「理发店」用英语完成沟通，主角是年轻女性。配角为理发师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；理发师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, I''d like this done today. (你好，想今天办好。)

理发师: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

年轻女性: How much will it cost? (大概多少钱？)

理发师: About fifty yuan. (大概五十。)

年轻女性: How long do I need to wait? (需要等多久？)

理发师: Around twenty minutes. (大概二十分钟。)

年轻女性: Can I pay when it''s finished? (做好再付可以吗？)

理发师: Yes, mobile pay is fine. (可以，手机付。)

年轻女性: I''ll wait here. Thanks! (我在这等。谢谢！)

理发师: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "理发店说清怎么剪，别再比划头发。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "理发店", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'ed1e38a8775271c54c9e0388e54fded0','理发店英语跟读🔑','生成要求：9:16竖屏，15-20秒。中国街边理发店座椅旁。语速明快。风格：生活服务风。

画面描述：老爷爷（约60+岁，花白短发，衬衫与马甲，慈祥耐心）正在「日常服务/理发店」场景中，在「理发店」用英语完成沟通，主角是老爷爷。配角为理发师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；理发师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, I''d like this done today. (你好，想今天办好。)

理发师: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

老爷爷: How much will it cost? (大概多少钱？)

理发师: About fifty yuan. (大概五十。)

老爷爷: How long do I need to wait? (需要等多久？)

理发师: Around twenty minutes. (大概二十分钟。)

老爷爷: Can I pay when it''s finished? (做好再付可以吗？)

理发师: Yes, mobile pay is fine. (可以，手机付。)

老爷爷: I''ll wait here. Thanks! (我在这等。谢谢！)

理发师: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "理发店说清怎么剪，别再比划头发。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "理发店", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'df5f5f81d87319e6b0349bcad32970af','英语·手机维修现场🔑','生成要求：9:16竖屏，15-20秒。中国手机维修店柜台。语速明快。风格：实用服务风。

画面描述：年轻男性（约20-30岁，短发整齐，休闲西装外套，干练友善）正在「日常服务/手机维修」场景中，在「手机维修」用英语完成沟通，主角是年轻男性。配角为维修师傅。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；维修师傅声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, I''d like this done today. (你好，想今天办好。)

维修师傅: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

年轻男性: How much will it cost? (大概多少钱？)

维修师傅: About fifty yuan. (大概五十。)

年轻男性: How long do I need to wait? (需要等多久？)

维修师傅: Around twenty minutes. (大概二十分钟。)

年轻男性: Can I pay when it''s finished? (做好再付可以吗？)

维修师傅: Yes, mobile pay is fine. (可以，手机付。)

年轻男性: I''ll wait here. Thanks! (我在这等。谢谢！)

维修师傅: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "手机维修店说明故障，师傅听得懂。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "手机维修", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'66b353bbda65871c55ddb59f51746373','手机维修开口说英语🔑','生成要求：9:16竖屏，15-20秒。中国手机维修店柜台。语速明快。风格：实用服务风。

画面描述：小女孩（约6-10岁，马尾辫配粉色发卡，浅色短袖连衣裙，活泼自然）正在「日常服务/手机维修」场景中，在「手机维修」用英语完成沟通，主角是小女孩。配角为维修师傅。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；维修师傅声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, I''d like this done today. (你好，想今天办好。)

维修师傅: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

小女孩: How much will it cost? (大概多少钱？)

维修师傅: About fifty yuan. (大概五十。)

小女孩: How long do I need to wait? (需要等多久？)

维修师傅: Around twenty minutes. (大概二十分钟。)

小女孩: Can I pay when it''s finished? (做好再付可以吗？)

维修师傅: Yes, mobile pay is fine. (可以，手机付。)

小女孩: I''ll wait here. Thanks! (我在这等。谢谢！)

维修师傅: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "手机维修店说明故障，师傅听得懂。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "手机维修", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d0bc98a0977003c68f3742df2b82ad90','干洗店开口说英语🔑','生成要求：9:16竖屏，15-20秒。中国干洗店收衣台。语速明快。风格：日常服务风。

画面描述：中年女性（约35-50岁，齐肩短发，得体风衣与平底鞋，沉稳耐心）正在「日常服务/干洗店」场景中，在「干洗店」用英语完成沟通，主角是中年女性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

中年女性: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

中年女性: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

中年女性: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

中年女性: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "干洗店交代面料污渍，收衣就能说。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "干洗店", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'986d624745e2b5a26e56a874b2380696','小男孩版干洗店英语🔑','生成要求：9:16竖屏，15-20秒。中国干洗店收衣台。语速明快。风格：日常服务风。

画面描述：小男孩（约6-10岁，短发，蓝白条纹T恤与运动鞋，好奇爱提问）正在「日常服务/干洗店」场景中，在「干洗店」用英语完成沟通，主角是小男孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

小男孩: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

小男孩: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

小男孩: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

小男孩: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "干洗店交代面料污渍，收衣就能说。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "干洗店", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'16be9ed87555f842c5f12359c30835d1','跟读|快递驿站英语🔑','生成要求：9:16竖屏，15-20秒。中国快递驿站货架前。语速明快。风格：邻里烟火风。

画面描述：中年男性（约35-50岁，短发微白，衬衫与夹克，可靠亲切）正在「日常服务/快递驿站」场景中，在「快递驿站」用英语完成沟通，主角是中年男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

中年男性: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

中年男性: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

中年男性: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

中年男性: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "驿站取件排队沟通，短句超好用。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "快递驿站", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'27169f908250ea75b7c29d0f0e56def7','快递驿站情景英语🔑','生成要求：9:16竖屏，15-20秒。中国快递驿站货架前。语速明快。风格：邻里烟火风。

画面描述：少女（约15-18岁，长直发，校服白衬衫深蓝裙，礼貌大方）正在「日常服务/快递驿站」场景中，在「快递驿站」用英语完成沟通，主角是少女。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

少女: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

少女: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

少女: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

少女: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "驿站取件排队沟通，短句超好用。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "快递驿站", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'260d1be47feb0b7cc52890f3f91a6c7b','生活英语·物业报修🔑','生成要求：9:16竖屏，15-20秒。中国小区物业前台。语速明快。风格：邻里实用风。

画面描述：老奶奶（约60+岁，花白短发，碎花布衣与薄外套，笑容温和）正在「日常服务/物业报修」场景中，在「物业报修」用英语完成沟通，主角是老奶奶。配角为物业。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；物业声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, I''d like this done today. (你好，想今天办好。)

物业: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

老奶奶: How much will it cost? (大概多少钱？)

物业: About fifty yuan. (大概五十。)

老奶奶: How long do I need to wait? (需要等多久？)

物业: Around twenty minutes. (大概二十分钟。)

老奶奶: Can I pay when it''s finished? (做好再付可以吗？)

物业: Yes, mobile pay is fine. (可以，手机付。)

老奶奶: I''ll wait here. Thanks! (我在这等。谢谢！)

物业: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "物业报修描述问题，小区生活刚需。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "物业报修", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'7b853f8e497d63c8e1af59442594a3ec','少年的物业报修英语🔑','生成要求：9:16竖屏，15-20秒。中国小区物业前台。语速明快。风格：邻里实用风。

画面描述：少年（约15-18岁，短发清爽，运动服套装，礼貌稳重）正在「日常服务/物业报修」场景中，在「物业报修」用英语完成沟通，主角是少年。配角为物业。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；物业声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, I''d like this done today. (你好，想今天办好。)

物业: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

少年: How much will it cost? (大概多少钱？)

物业: About fifty yuan. (大概五十。)

少年: How long do I need to wait? (需要等多久？)

物业: Around twenty minutes. (大概二十分钟。)

少年: Can I pay when it''s finished? (做好再付可以吗？)

物业: Yes, mobile pay is fine. (可以，手机付。)

少年: I''ll wait here. Thanks! (我在这等。谢谢！)

物业: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "物业报修描述问题，小区生活刚需。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "物业报修", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'46733f5549b058fd4a8d4bea65bf9f62','老爷爷·充电站英语🔑','生成要求：9:16竖屏，15-20秒。中国路边新能源充电站。语速明快。风格：现代生活风。

画面描述：老爷爷（约62-75岁，白发寸头，中山装式外套，精神矍铄）正在「日常服务/充电站」场景中，在「充电站」用英语完成沟通，主角是老爷爷。配角为站务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；站务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老爷爷: Hi, I''d like this done today. (你好，想今天办好。)

站务员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

老爷爷: How much will it cost? (大概多少钱？)

站务员: About fifty yuan. (大概五十。)

老爷爷: How long do I need to wait? (需要等多久？)

站务员: Around twenty minutes. (大概二十分钟。)

老爷爷: Can I pay when it''s finished? (做好再付可以吗？)

站务员: Yes, mobile pay is fine. (可以，手机付。)

老爷爷: I''ll wait here. Thanks! (我在这等。谢谢！)

站务员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "充电站问空位怎么充，出行新场景。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "充电站", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'8aaa4bdf6225837d7a91cff27676a29e','充电站英语跟读🔑','生成要求：9:16竖屏，15-20秒。中国路边新能源充电站。语速明快。风格：现代生活风。

画面描述：年轻女性（约22-28岁，低盘发，白衬衫配西裤，干练通勤）正在「日常服务/充电站」场景中，在「充电站」用英语完成沟通，主角是年轻女性。配角为站务员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；站务员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻女性: Hi, I''d like this done today. (你好，想今天办好。)

站务员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

年轻女性: How much will it cost? (大概多少钱？)

站务员: About fifty yuan. (大概五十。)

年轻女性: How long do I need to wait? (需要等多久？)

站务员: Around twenty minutes. (大概二十分钟。)

年轻女性: Can I pay when it''s finished? (做好再付可以吗？)

站务员: Yes, mobile pay is fine. (可以，手机付。)

年轻女性: I''ll wait here. Thanks! (我在这等。谢谢！)

站务员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "充电站问空位怎么充，出行新场景。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "充电站", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'d111967c07a3cfe8aa840636ca15dbe2','英语·照相馆现场🔑','生成要求：9:16竖屏，15-20秒。中国证件照相馆。语速明快。风格：生活实用风。

画面描述：小女孩（约6-10岁，戴遮阳帽与小书包，格子短袖短裤，好奇张望）正在「日常服务/照相馆」场景中，在「照相馆」用英语完成沟通，主角是小女孩。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小女孩: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

小女孩: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

小女孩: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

小女孩: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

小女孩: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "证件照怎么拍，跟店员一次说清。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "照相馆", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'a9dca7b9585a4db0eb12a44168444d4f','照相馆开口说英语🔑','生成要求：9:16竖屏，15-20秒。中国证件照相馆。语速明快。风格：生活实用风。

画面描述：年轻男性（约22-28岁，微长碎发，连帽卫衣与牛仔裤，轻松）正在「日常服务/照相馆」场景中，在「照相馆」用英语完成沟通，主角是年轻男性。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

年轻男性: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

年轻男性: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

年轻男性: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

年轻男性: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

年轻男性: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "证件照怎么拍，跟店员一次说清。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "照相馆", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'86013dd37f43404740bddd9b1378f8ab','配钥匙开口说英语🔑','生成要求：9:16竖屏，15-20秒。中国社区配钥匙摊。语速明快。风格：市井实用风。

画面描述：小男孩（约7-9岁，戴鸭舌帽，迷彩短裤与背心，活力十足）正在「日常服务/配钥匙」场景中，在「配钥匙」用英语完成沟通，主角是小男孩。配角为师傅。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；师傅声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

小男孩: Hi, I''d like this done today. (你好，想今天办好。)

师傅: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

小男孩: How much will it cost? (大概多少钱？)

师傅: About fifty yuan. (大概五十。)

小男孩: How long do I need to wait? (需要等多久？)

师傅: Around twenty minutes. (大概二十分钟。)

小男孩: Can I pay when it''s finished? (做好再付可以吗？)

师傅: Yes, mobile pay is fine. (可以，手机付。)

小男孩: I''ll wait here. Thanks! (我在这等。谢谢！)

师傅: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "配钥匙问能不能配、多久好，超生活。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "配钥匙", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b5c6d33d12e5743329515a7e660dccef','中年女性版配钥匙英语🔑','生成要求：9:16竖屏，15-20秒。中国社区配钥匙摊。语速明快。风格：市井实用风。

画面描述：中年女性（约38-48岁，微卷中长发，针织衫配长裙，温和）正在「日常服务/配钥匙」场景中，在「配钥匙」用英语完成沟通，主角是中年女性。配角为师傅。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；师傅声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年女性: Hi, I''d like this done today. (你好，想今天办好。)

师傅: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

中年女性: How much will it cost? (大概多少钱？)

师傅: About fifty yuan. (大概五十。)

中年女性: How long do I need to wait? (需要等多久？)

师傅: Around twenty minutes. (大概二十分钟。)

中年女性: Can I pay when it''s finished? (做好再付可以吗？)

师傅: Yes, mobile pay is fine. (可以，手机付。)

中年女性: I''ll wait here. Thanks! (我在这等。谢谢！)

师傅: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "配钥匙问能不能配、多久好，超生活。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "配钥匙", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'b06da828c50aefe702963e29f3a2ca4c','跟读|眼镜店英语🔑','生成要求：9:16竖屏，15-20秒。中国眼镜店验光台旁。语速明快。风格：生活服务风。

画面描述：少女（约16-18岁，低马尾，浅色针织开衫与牛仔裤，安静文雅）正在「日常服务/眼镜店」场景中，在「眼镜店」用英语完成沟通，主角是少女。配角为验光师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；验光师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少女: Hi, I''d like this done today. (你好，想今天办好。)

验光师: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

少女: How much will it cost? (大概多少钱？)

验光师: About fifty yuan. (大概五十。)

少女: How long do I need to wait? (需要等多久？)

验光师: Around twenty minutes. (大概二十分钟。)

少女: Can I pay when it''s finished? (做好再付可以吗？)

验光师: Yes, mobile pay is fine. (可以，手机付。)

少女: I''ll wait here. Thanks! (我在这等。谢谢！)

验光师: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "验光配镜沟通，度数清晰表达。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "眼镜店", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6220d26a0ad8156c3aa93790271e09d4','眼镜店情景英语🔑','生成要求：9:16竖屏，15-20秒。中国眼镜店验光台旁。语速明快。风格：生活服务风。

画面描述：中年男性（约38-48岁，寸头，Polo衫与卡其裤，朴实）正在「日常服务/眼镜店」场景中，在「眼镜店」用英语完成沟通，主角是中年男性。配角为验光师。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；验光师声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

中年男性: Hi, I''d like this done today. (你好，想今天办好。)

验光师: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

中年男性: How much will it cost? (大概多少钱？)

验光师: About fifty yuan. (大概五十。)

中年男性: How long do I need to wait? (需要等多久？)

验光师: Around twenty minutes. (大概二十分钟。)

中年男性: Can I pay when it''s finished? (做好再付可以吗？)

验光师: Yes, mobile pay is fine. (可以，手机付。)

中年男性: I''ll wait here. Thanks! (我在这等。谢谢！)

验光师: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "验光配镜沟通，度数清晰表达。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "眼镜店", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'072974ee84b6fbe105c377a869c388b3','生活英语·宠物店🔑','生成要求：9:16竖屏，15-20秒。中国宠物店货架前。语速明快。风格：萌宠生活风。

画面描述：少年（约16-18岁，微乱短发，连帽卫衣与牛仔裤，随性）正在「日常服务/宠物店」场景中，在「宠物店」用英语完成沟通，主角是少年。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

少年: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

少年: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

少年: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

少年: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

少年: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "宠物店买粮问成分，铲屎官必备。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "宠物店", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3e7bc159b501ae570da97994151bf90d','老奶奶的宠物店英语🔑','生成要求：9:16竖屏，15-20秒。中国宠物店货架前。语速明快。风格：萌宠生活风。

画面描述：老奶奶（约62-75岁，盘发插簪，深色棉袄与布鞋，慈祥）正在「日常服务/宠物店」场景中，在「宠物店」用英语完成沟通，主角是老奶奶。配角为店员。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；店员声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

老奶奶: Hi, I''d like this done today. (你好，想今天办好。)

店员: Let me check. What exactly do you need? (我看一下，您具体需要什么？)

老奶奶: How much will it cost? (大概多少钱？)

店员: About fifty yuan. (大概五十。)

老奶奶: How long do I need to wait? (需要等多久？)

店员: Around twenty minutes. (大概二十分钟。)

老奶奶: Can I pay when it''s finished? (做好再付可以吗？)

店员: Yes, mobile pay is fine. (可以，手机付。)

老奶奶: I''ll wait here. Thanks! (我在这等。谢谢！)

店员: Ready soon. Please take a seat. (马上好，请坐。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "日常服务", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "宠物店买粮问成分，铲屎官必备。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "日常服务", "sceneSub": "宠物店", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'36313e61cf954625e8ec119ac8f7e56d','小女孩·楼顶菜园摘菜英语🏠','生成要求：9:16竖屏，15-20秒。中国城市楼顶菜园或小院，阳光明媚，绿意盎然。语速明快。风格：田园治愈风。

画面描述：小女孩（约7-9岁，齐肩发，条纹T恤配背带裤，笑容明亮）戴着遮阳帽提着小竹篮，正帮爷爷摘小番茄；爷爷在旁边指着藤蔓，她看到一个大番茄惊喜地笑起来。配角为爷爷。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；爷爷声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爷爷: Honey, can you pass me the basket? (宝贝，能把篮子递给我吗？)

小女孩: Here you are, Grandpa. It''s so sunny today. (给你，爷爷。今天太阳真好。)

爷爷: Yes. Be careful of the sharp leaves. (是啊。小心那些尖锐的叶子。)

小女孩: I will. Are these tomatoes ready to pick? (我会的。这些西红柿熟了吗？)

爷爷: Yes, pick the red ones. (熟了，挑红色的摘。)

小女孩: Wow, this one is huge! (哇，这个好大！)

爷爷: Put it in gently so it doesn''t break. (轻点放，别弄破了。)

小女孩: I got it. What are we doing with them? (我知道了。我们用它们做什么？)

爷爷: I''ll make you scrambled eggs with tomatoes. (我给你做番茄炒蛋。)

小女孩: Awesome! My favorite dish! (太棒了！我最喜欢的一道菜！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "楼顶摘番茄也能练英语，亲情向对话超治愈。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "楼顶菜园摘菜", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'707e892e852e7583065beba3b912cf4f','楼顶菜园摘菜英语跟读🏠','生成要求：9:16竖屏，15-20秒。中国城市楼顶菜园或小院，阳光明媚，绿意盎然。语速明快。风格：田园治愈风。

画面描述：小男孩（约6-8岁，圆寸头，校服式白衬衫卷袖，书包斜挎）提着小竹篮帮爷爷在菜园摘菜、浇水。配角为爷爷。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；爷爷声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爷爷: Pass me the scissors, buddy. (把剪刀递给我，伙计。)

小男孩: Here, Grandpa. Can I cut this cucumber? (给你，爷爷。我能剪这根黄瓜吗？)

爷爷: Yes, but hold it carefully. (可以，但要拿稳。)

小男孩: It''s so green and fresh! (好绿好新鲜！)

爷爷: We''ll cook it for lunch. (我们午饭就炒它。)

小男孩: Should I put them in the basket? (要放进篮子里吗？)

爷爷: Yes. Don''t crush the soft ones. (对，别把软的压坏了。)

小男孩: Got it. What''s next? (好的。下一步做什么？)

爷爷: Water the chili plants a little. (给辣椒苗浇一点水。)

小男孩: Okay! Working with you is fun. (好！和你一起干活真有意思。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "楼顶摘番茄也能练英语，亲情向对话超治愈。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "楼顶菜园摘菜", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'23f35c1de561fea7c8bcd60965e4bfba','英语·厨房洗菜现场🏠','生成要求：9:16竖屏，15-20秒。中国家庭厨房，灶台干净，窗外有日光。语速明快。风格：温馨家常风。

画面描述：老奶奶（约60-70岁，齐耳白发，针织开衫与长裤，清爽）正在「家庭互助/厨房洗菜」场景中，在「厨房洗菜」用英语完成沟通，主角是老奶奶。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: Can you wash these greens for me? (能帮我洗一下这些青菜吗？)

老奶奶: Sure. Should I use cold water? (好的。用冷水吗？)

妈妈: Yes. Rub them gently. (对，轻轻搓洗。)

老奶奶: There are tiny bugs on this leaf! (这片叶子上有小虫！)

妈妈: Rinse them off carefully. (仔细冲掉就好。)

老奶奶: Okay. How many bowls do we need? (好。我们需要几碗？)

妈妈: Two will be enough tonight. (今晚两碗就够了。)

老奶奶: I finished. Where should I put them? (我洗完了。放哪儿？)

妈妈: In that basket. Thank you. (放那个篮子里。谢谢。)

老奶奶: You''re welcome! What''s for dinner? (不客气！晚饭吃什么？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "厨房洗菜家常对话，开口就很生活。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "厨房洗菜", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'12bad090f2850d5304f92f20eb42fa56','厨房洗菜开口说英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭厨房，灶台干净，窗外有日光。语速明快。风格：温馨家常风。

画面描述：少年（约15-17岁，侧分头，白衬衫校服裤，书包双肩）正在「家庭互助/厨房洗菜」场景中，在「厨房洗菜」用英语完成沟通，主角是少年。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: Can you wash these greens for me? (能帮我洗一下这些青菜吗？)

少年: Sure. Should I use cold water? (好的。用冷水吗？)

妈妈: Yes. Rub them gently. (对，轻轻搓洗。)

少年: There are tiny bugs on this leaf! (这片叶子上有小虫！)

妈妈: Rinse them off carefully. (仔细冲掉就好。)

少年: Okay. How many bowls do we need? (好。我们需要几碗？)

妈妈: Two will be enough tonight. (今晚两碗就够了。)

少年: I finished. Where should I put them? (我洗完了。放哪儿？)

妈妈: In that basket. Thank you. (放那个篮子里。谢谢。)

少年: You''re welcome! What''s for dinner? (不客气！晚饭吃什么？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "厨房洗菜家常对话，开口就很生活。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "厨房洗菜", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'dea6d186aae7777861566b4be97504f6','叠衣物开口说英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭客厅沙发旁，衣服整齐堆放。语速明快。风格：生活记录风。

画面描述：老爷爷（约60-70岁，银框眼镜，羊毛衫与长裤，温和）正在「家庭互助/叠衣物」场景中，在「叠衣物」用英语完成沟通，主角是老爷爷。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: Let''s fold these clothes together. (我们一起把这些衣服叠好。)

老爷爷: Okay. How do I fold a T-shirt? (好。T恤怎么叠？)

妈妈: Sleeve in, then in half. (袖子收进去，再对折。)

老爷爷: Like this? (像这样？)

妈妈: Perfect. Now the pants. (完美。接下来叠裤子。)

老爷爷: Where should the socks go? (袜子放哪儿？)

妈妈: In that small basket. (放那个小篮子里。)

老爷爷: I''m getting faster! (我越来越快了！)

妈妈: Yes you are. Great job. (是啊，真棒。)

老爷爷: Can we play after this? (叠完能玩一会儿吗？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客厅叠衣物互助，温柔跟读练习。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "叠衣物", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'bdfde73f8af1d9a16233fee170c05ae5','年轻女性版叠衣物英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭客厅沙发旁，衣服整齐堆放。语速明快。风格：生活记录风。

画面描述：年轻女性（约20-26岁，齐肩波浪发，针织衫与牛仔裙，柔和）正在「家庭互助/叠衣物」场景中，在「叠衣物」用英语完成沟通，主角是年轻女性。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: Let''s fold these clothes together. (我们一起把这些衣服叠好。)

年轻女性: Okay. How do I fold a T-shirt? (好。T恤怎么叠？)

妈妈: Sleeve in, then in half. (袖子收进去，再对折。)

年轻女性: Like this? (像这样？)

妈妈: Perfect. Now the pants. (完美。接下来叠裤子。)

年轻女性: Where should the socks go? (袜子放哪儿？)

妈妈: In that small basket. (放那个小篮子里。)

年轻女性: I''m getting faster! (我越来越快了！)

妈妈: Yes you are. Great job. (是啊，真棒。)

年轻女性: Can we play after this? (叠完能玩一会儿吗？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "客厅叠衣物互助，温柔跟读练习。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "叠衣物", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'7c1363d3947780a5e00054bdba461e66','跟读|浇花浇水英语🏠','生成要求：9:16竖屏，15-20秒。中国家中阳台，花盆与喷壶。语速明快。风格：清新治愈风。

画面描述：小女孩（约6-8岁，双麻花辫，浅色防晒衣与运动鞋，蹦跳感）正在「家庭互助/浇花浇水」场景中，在「浇花浇水」用英语完成沟通，主角是小女孩。配角为奶奶。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；奶奶声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

奶奶: The soil looks dry. Let''s water them. (土有点干了，我们浇点水。)

小女孩: How much water for this pot? (这盆要浇多少？)

奶奶: Just a little. Don''t flood it. (一点点就好，别浇太多。)

小女孩: The leaves are shiny now! (叶子现在亮晶晶的！)

奶奶: Yes. Plants love gentle care. (是啊，植物喜欢温柔照料。)

小女孩: Should I water the basil too? (罗勒也要浇吗？)

奶奶: Yes, and check for dry leaves. (要，再看看有没有干叶子。)

小女孩: I found one. Can I pick it? (我找到一片，能摘吗？)

奶奶: Go ahead. Put it in the bag. (可以，放袋子里。)

小女孩: Gardening with you is so nice. (和你一起打理植物真好。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "阳台浇花对话，生活感满满。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "浇花浇水", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'080dad327ce7728c97f50f445bd35fcc','浇花浇水情景英语🏠','生成要求：9:16竖屏，15-20秒。中国家中阳台，花盆与喷壶。语速明快。风格：清新治愈风。

画面描述：年轻男性（约24-30岁，侧分头，白衬衫卷袖与斜挎包，利落）正在「家庭互助/浇花浇水」场景中，在「浇花浇水」用英语完成沟通，主角是年轻男性。配角为奶奶。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；奶奶声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

奶奶: The soil looks dry. Let''s water them. (土有点干了，我们浇点水。)

年轻男性: How much water for this pot? (这盆要浇多少？)

奶奶: Just a little. Don''t flood it. (一点点就好，别浇太多。)

年轻男性: The leaves are shiny now! (叶子现在亮晶晶的！)

奶奶: Yes. Plants love gentle care. (是啊，植物喜欢温柔照料。)

年轻男性: Should I water the basil too? (罗勒也要浇吗？)

奶奶: Yes, and check for dry leaves. (要，再看看有没有干叶子。)

年轻男性: I found one. Can I pick it? (我找到一片，能摘吗？)

奶奶: Go ahead. Put it in the bag. (可以，放袋子里。)

年轻男性: Gardening with you is so nice. (和你一起打理植物真好。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "阳台浇花对话，生活感满满。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "浇花浇水", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'486bcd648c54a6f11bd1aeaabd7a0d2c','生活英语·收拾餐桌🏠','生成要求：9:16竖屏，15-20秒。中国家庭餐桌旁，饭后碗筷。语速明快。风格：家常烟火风。

画面描述：小男孩（约8-10岁，刺猬头，红色运动套装，额头微汗）正在「家庭互助/收拾餐桌」场景中，在「收拾餐桌」用英语完成沟通，主角是小男孩。配角为爸爸。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；爸爸声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爸爸: Dinner''s over. Let''s clear the table. (吃完了，我们收拾桌子吧。)

小男孩: I''ll take the bowls first. (我先拿碗。)

爸爸: Careful, that one is still hot. (小心，那个还烫。)

小男孩: Where do the chopsticks go? (筷子放哪儿？)

爸爸: In the holder by the sink. (放水槽边的筷子筒。)

小男孩: Should I wipe the table? (要我擦桌子吗？)

爸爸: Yes, please. Use this cloth. (好，用这块布。)

小男孩: All clean! (都干净了！)

爸爸: Teamwork. Thank you. (配合得很好，谢谢。)

小男孩: Anytime! What''s for fruit? (随时！吃什么水果？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "饭后收拾餐桌，家庭英语超自然。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "收拾餐桌", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'4c96abc5e2afad37830ebf402359137a','中年女性的收拾餐桌英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭餐桌旁，饭后碗筷。语速明快。风格：家常烟火风。

画面描述：中年女性（约35-45岁，利落短发，衬衫与西裤套装，干练）正在「家庭互助/收拾餐桌」场景中，在「收拾餐桌」用英语完成沟通，主角是中年女性。配角为爸爸。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；爸爸声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爸爸: Dinner''s over. Let''s clear the table. (吃完了，我们收拾桌子吧。)

中年女性: I''ll take the bowls first. (我先拿碗。)

爸爸: Careful, that one is still hot. (小心，那个还烫。)

中年女性: Where do the chopsticks go? (筷子放哪儿？)

爸爸: In the holder by the sink. (放水槽边的筷子筒。)

中年女性: Should I wipe the table? (要我擦桌子吗？)

爸爸: Yes, please. Use this cloth. (好，用这块布。)

中年女性: All clean! (都干净了！)

爸爸: Teamwork. Thank you. (配合得很好，谢谢。)

中年女性: Anytime! What''s for fruit? (随时！吃什么水果？)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "饭后收拾餐桌，家庭英语超自然。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "收拾餐桌", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c12b66fee6d25d9eb9f1841a228bb824','少女·喂狗喂鱼英语🏠','生成要求：9:16竖屏，15-20秒。中国家中客厅，宠物食盆与鱼缸。语速明快。风格：萌宠治愈风。

画面描述：少女（约15-17岁，齐肩黑发，卫衣配帆布鞋，校园休闲感）正在「家庭互助/喂狗喂鱼」场景中，在「喂狗喂鱼」用英语完成沟通，主角是少女。配角为姐姐。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少女声音温柔清晰；姐姐声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

姐姐: It''s feeding time. Can you help? (到喂食时间了，能帮忙吗？)

少女: I''ll fill the dog''s bowl. (我来给狗碗添粮。)

姐姐: Not too much. He already ate snacks. (别太多，他刚吃过零食。)

少女: How many pellets for the fish? (鱼要几粒饲料？)

姐姐: Three is enough. (三粒就够。)

少女: Look, they''re swimming up! (看，它们游上来了！)

姐姐: They know your voice. (它们认得你的声音。)

少女: Can I refill the water bowl? (我能把水碗续满吗？)

姐姐: Yes. Fresh water is important. (可以，新鲜的水很重要。)

少女: Done! Our pets look happy. (好了！宠物们看起来好开心。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "少女"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "喂狗喂鱼分工，家务场景口语。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "喂狗喂鱼", "protagonist": "少女", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'0e079913b2b272a563bbc199f769b4c3','喂狗喂鱼英语跟读🏠','生成要求：9:16竖屏，15-20秒。中国家中客厅，宠物食盆与鱼缸。语速明快。风格：萌宠治愈风。

画面描述：中年男性（约40-50岁，黑框眼镜，深色西装外套便装化，稳重）正在「家庭互助/喂狗喂鱼」场景中，在「喂狗喂鱼」用英语完成沟通，主角是中年男性。配角为姐姐。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年男性声音沉稳有力；姐姐声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

姐姐: It''s feeding time. Can you help? (到喂食时间了，能帮忙吗？)

中年男性: I''ll fill the dog''s bowl. (我来给狗碗添粮。)

姐姐: Not too much. He already ate snacks. (别太多，他刚吃过零食。)

中年男性: How many pellets for the fish? (鱼要几粒饲料？)

姐姐: Three is enough. (三粒就够。)

中年男性: Look, they''re swimming up! (看，它们游上来了！)

姐姐: They know your voice. (它们认得你的声音。)

中年男性: Can I refill the water bowl? (我能把水碗续满吗？)

姐姐: Yes. Fresh water is important. (可以，新鲜的水很重要。)

中年男性: Done! Our pets look happy. (好了！宠物们看起来好开心。)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "中年男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "喂狗喂鱼分工，家务场景口语。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "喂狗喂鱼", "protagonist": "中年男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'c7331c4e59d5030ed9f852f51821a74a','英语·擦窗台现场🏠','生成要求：9:16竖屏，15-20秒。中国家中窗台，抹布与阳光。语速明快。风格：干净治愈风。

画面描述：少年（约16-18岁，寸头，深色夹克配帆布鞋，干练）正在「家庭互助/擦窗台」场景中，在「擦窗台」用英语完成沟通，主角是少年。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。少年声音偏少年感、清晰；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: The windowsill is dusty. Let''s clean it. (窗台有灰，我们擦一擦。)

少年: I brought the cloth and spray. (我拿来抹布和喷雾了。)

妈妈: Spray a little, then wipe. (喷一点，然后擦。)

少年: It''s shiny already! (已经亮了！)

妈妈: Don''t forget the corners. (别忘了边角。)

少年: Got them. Should I open the window? (弄好了。要开窗吗？)

妈妈: Yes, let the fresh air in. (开，让新鲜空气进来。)

少年: It smells so nice outside. (外面好香。)

妈妈: Great work today. (今天干得漂亮。)

少年: Cleaning can be fun! (打扫也可以很好玩！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "少年"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "擦窗台帮忙，家务英语跟读。听感干净，适合反复跟读。#英语口语 #生活英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "擦窗台", "protagonist": "少年", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'3d5570b812dbb07b3c80fc031d1ed735','擦窗台开口说英语🏠','生成要求：9:16竖屏，15-20秒。中国家中窗台，抹布与阳光。语速明快。风格：干净治愈风。

画面描述：老奶奶（约65-78岁，头巾轻裹，格子外套与布包，家常）正在「家庭互助/擦窗台」场景中，在「擦窗台」用英语完成沟通，主角是老奶奶。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老奶奶声音慈祥和缓；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: The windowsill is dusty. Let''s clean it. (窗台有灰，我们擦一擦。)

老奶奶: I brought the cloth and spray. (我拿来抹布和喷雾了。)

妈妈: Spray a little, then wipe. (喷一点，然后擦。)

老奶奶: It''s shiny already! (已经亮了！)

妈妈: Don''t forget the corners. (别忘了边角。)

老奶奶: Got them. Should I open the window? (弄好了。要开窗吗？)

妈妈: Yes, let the fresh air in. (开，让新鲜空气进来。)

老奶奶: It smells so nice outside. (外面好香。)

妈妈: Great work today. (今天干得漂亮。)

老奶奶: Cleaning can be fun! (打扫也可以很好玩！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "老奶奶"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "擦窗台帮忙，家务英语跟读。中国街头真实感强，收藏慢慢练。#英语听力 #实用英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "擦窗台", "protagonist": "老奶奶", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'6032ba6d8143d0adc1101d4183520af9','买早餐回来开口说英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭门口，豆浆油条纸袋。语速明快。风格：人间烟火风。

画面描述：年轻女性（约24-30岁，高马尾，运动夹克与瑜伽裤，轻快）正在「家庭互助/买早餐回来」场景中，在「买早餐回来」用英语完成沟通，主角是年轻女性。配角为爸爸。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻女性声音清亮自然；爸爸声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爸爸: Did you get the soy milk and youtiao? (豆浆油条买到了吗？)

年轻女性: Yes! Still warm. (买到了！还热乎着。)

爸爸: Wonderful. Put them on the table. (太好了，放到桌上。)

年轻女性: I also got one boiled egg. (我还买了个茶叶蛋。)

爸爸: Smart choice. Who paid? (聪明。谁付钱的？)

年轻女性: I used the family QR code. (我用了家里的收款码。)

爸爸: Perfect. Thank you for helping. (完美，谢谢你帮忙。)

年轻女性: Can we eat together now? (现在可以一起吃吗？)

爸爸: Of course. Sit down. (当然，坐下吧。)

年轻女性: Breakfast smells amazing! (早餐好香！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "年轻女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "买豆浆油条回家，门口交接超日常。从打招呼到道谢串成完整对话。#英语情景对话 #日常英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "买早餐回来", "protagonist": "年轻女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'f67c65b7f0709519bfd11b68056333b8','老爷爷版买早餐回来英语🏠','生成要求：9:16竖屏，15-20秒。中国家庭门口，豆浆油条纸袋。语速明快。风格：人间烟火风。

画面描述：老爷爷（约65-78岁，花白寸发，深蓝夹克与布帽，朴实）正在「家庭互助/买早餐回来」场景中，在「买早餐回来」用英语完成沟通，主角是老爷爷。配角为爸爸。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。老爷爷声音慈祥低沉；爸爸声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爸爸: Did you get the soy milk and youtiao? (豆浆油条买到了吗？)

老爷爷: Yes! Still warm. (买到了！还热乎着。)

爸爸: Wonderful. Put them on the table. (太好了，放到桌上。)

老爷爷: I also got one boiled egg. (我还买了个茶叶蛋。)

爸爸: Smart choice. Who paid? (聪明。谁付钱的？)

老爷爷: I used the family QR code. (我用了家里的收款码。)

爸爸: Perfect. Thank you for helping. (完美，谢谢你帮忙。)

老爷爷: Can we eat together now? (现在可以一起吃吗？)

爸爸: Of course. Sit down. (当然，坐下吧。)

老爷爷: Breakfast smells amazing! (早餐好香！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "老爷爷"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "买豆浆油条回家，门口交接超日常。反差视角更有记忆点，跟读不枯燥。#英语启蒙 #跟读练习", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "买早餐回来", "protagonist": "老爷爷", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'e715f56640c40066987b272c63bf1b5f','跟读|帮忙拿快递英语🏠','生成要求：9:16竖屏，15-20秒。中国小区快递柜旁。语速明快。风格：独立生活风。

画面描述：年轻男性（约20-26岁，寸头，运动外套与跑鞋，精神）正在「家庭互助/帮忙拿快递」场景中，在「帮忙拿快递」用英语完成沟通，主角是年轻男性。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。年轻男性声音稳重清晰；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: There''s a parcel at the locker. Can you get it? (快递柜有个包裹，你去取一下？)

年轻男性: Sure. What''s the pickup code? (好。取件码是多少？)

妈妈: It''s on my phone. I''ll send it. (在我手机上，我发给你。)

年轻男性: Got it. Which locker number? (收到了。几号柜？)

妈妈: A12. Be careful, it might be heavy. (A12。小心，可能有点重。)

年轻男性: I opened it. It''s a box of books. (打开了，是一箱书。)

妈妈: Great. Bring it upstairs, please. (太好了，请搬上楼。)

年轻男性: I''m at the door now. (我到门口了。)

妈妈: Thank you so much. (太谢谢了。)

年轻男性: No problem. Happy to help! (没事，乐意帮忙！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "年轻男性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "小区柜旁拿快递，短对话天天能用。一句一场景，碎片时间也能开口。#旅游英语 #人间烟火", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "帮忙拿快递", "protagonist": "年轻男性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'9588afbf50db08736a0978ad5d7a5b09','帮忙拿快递情景英语🏠','生成要求：9:16竖屏，15-20秒。中国小区快递柜旁。语速明快。风格：独立生活风。

画面描述：小女孩（约8-10岁，戴棒球帽，卡通印花短袖，背迷你双肩包）正在「家庭互助/帮忙拿快递」场景中，在「帮忙拿快递」用英语完成沟通，主角是小女孩。配角为妈妈。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小女孩声音清脆甜美；妈妈声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

妈妈: There''s a parcel at the locker. Can you get it? (快递柜有个包裹，你去取一下？)

小女孩: Sure. What''s the pickup code? (好。取件码是多少？)

妈妈: It''s on my phone. I''ll send it. (在我手机上，我发给你。)

小女孩: Got it. Which locker number? (收到了。几号柜？)

妈妈: A12. Be careful, it might be heavy. (A12。小心，可能有点重。)

小女孩: I opened it. It''s a box of books. (打开了，是一箱书。)

妈妈: Great. Bring it upstairs, please. (太好了，请搬上楼。)

小女孩: I''m at the door now. (我到门口了。)

妈妈: Thank you so much. (太谢谢了。)

小女孩: No problem. Happy to help! (没事，乐意帮忙！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小女孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "小区柜旁拿快递，短对话天天能用。问路点餐购物都能迁移着用。#生活词汇 #开口说英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "帮忙拿快递", "protagonist": "小女孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'f419fedb94758e0f3df948d9a2bfc311','生活英语·陪爷爷散步🏠','生成要求：9:16竖屏，15-20秒。中国小区林荫道，傍晚余晖。语速明快。风格：亲情治愈风。

画面描述：中年女性（约40-50岁，低盘发，丝巾与大衣，端庄）正在「家庭互助/陪爷爷散步」场景中，在「陪爷爷散步」用英语完成沟通，主角是中年女性。配角为爷爷。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。中年女性声音温和成熟；爷爷声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爷爷: The evening air feels nice. Shall we walk? (傍晚空气真好，我们走走？)

中年女性: Yes! Let''s go slowly. (好！我们慢慢走。)

爷爷: Hold my arm if the path is uneven. (路不平就挽着我。)

中年女性: Look at those birds in the trees. (看树上那些鸟。)

爷爷: They come here every dusk. (它们每天傍晚都来。)

中年女性: Should we rest on that bench? (要在那张椅子上休息吗？)

爷爷: Just for a minute. Then we head back. (就一会儿，然后回家。)

中年女性: I like walking with you, Grandpa. (我喜欢和你一起散步，爷爷。)

爷爷: Me too. You make me feel young. (我也是，你让我觉得年轻。)

中年女性: Let''s come again tomorrow! (我们明天再来！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "中年女性"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "傍晚散步暖心对话，听完想开口。学完马上能用，比课本更贴生活。#实用英语 #通勤练英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "陪爷爷散步", "protagonist": "中年女性", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON)),
(@user_id,'f89cb79b5871144030dd79ce0ccebf65','小男孩的陪爷爷散步英语🏠','生成要求：9:16竖屏，15-20秒。中国小区林荫道，傍晚余晖。语速明快。风格：亲情治愈风。

画面描述：小男孩（约6-9岁，侧分短发，格子衬衫配牛仔裤，乖巧）正在「家庭互助/陪爷爷散步」场景中，在「陪爷爷散步」用英语完成沟通，主角是小男孩。配角为爷爷。画面必须能看出在中国（中文招牌、指示、街景或家居陈设）。中景/过肩为主，人脸清晰，避免真实品牌Logo。结尾友好收束（微笑/点头/挥手）。

声音要求：语音仅使用英文。小男孩声音清亮活泼；爷爷声音自然清晰、符合身份。

文本与字幕要求：画面上方居中显示双语字幕，英文字幕在上（加粗白字），简体中文释义在下（小号浅绿字），每句停留约1.5秒。字幕与口型对齐；中文必须为简体。

对话文案（语音仅读英文）：

爷爷: The evening air feels nice. Shall we walk? (傍晚空气真好，我们走走？)

小男孩: Yes! Let''s go slowly. (好！我们慢慢走。)

爷爷: Hold my arm if the path is uneven. (路不平就挽着我。)

小男孩: Look at those birds in the trees. (看树上那些鸟。)

爷爷: They come here every dusk. (它们每天傍晚都来。)

小男孩: Should we rest on that bench? (要在那张椅子上休息吗？)

爷爷: Just for a minute. Then we head back. (就一会儿，然后回家。)

小男孩: I like walking with you, Grandpa. (我喜欢和你一起散步，爷爷。)

爷爷: Me too. You make me feel young. (我也是，你让我觉得年轻。)

小男孩: Let''s come again tomorrow! (我们明天再来！)

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。',CAST('["抖音", "视频号", "小红书", "英语", "英语情景对话", "家庭互助", "小男孩"]' AS JSON),CAST('[]' AS JSON),'DRAFT','NORMAL',CAST('{"summary": "傍晚散步暖心对话，听完想开口。烟火气对话，听完想立刻试开口。#英语口语 #烟火气英语", "source": "seed_sql", "importBatchId": "en-cn-dialogue-china-200", "sceneCategory": "家庭互助", "sceneSub": "陪爷爷散步", "protagonist": "小男孩", "durationSec": "15-20", "aspectRatio": "9:16", "dialogueLines": 10, "subtitle": "EN bold white top / ZH-Hans light-green bottom", "audio": "EN only", "for": "doubao"}' AS JSON))
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`),`prompt_text`=VALUES(`prompt_text`),`config_tags`=VALUES(`config_tags`),`manual_tags`=VALUES(`manual_tags`),`publish_status`=VALUES(`publish_status`),`status`=VALUES(`status`),`ext_info`=VALUES(`ext_info`);

-- CONFIG标签25；提示词200；含主角。
