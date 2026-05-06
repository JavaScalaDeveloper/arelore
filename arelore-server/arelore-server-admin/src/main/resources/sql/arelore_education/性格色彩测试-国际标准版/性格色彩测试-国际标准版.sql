USE arelore_education;

-- 如需重置
# DELETE FROM user_detection_question WHERE type_code = 'COLOR4_INTL_V1';
# DELETE FROM user_detection_type WHERE type_code = 'COLOR4_INTL_V1';

INSERT INTO user_detection_type
    (modifier, type_code, type_name, type_description, extra_info)
VALUES ('system',
        'COLOR4_INTL_V1',
        '性格色彩测试-国际标准版',
        '基于红/黄/蓝/绿四维行为风格的职业与沟通偏好测评',
        JSON_OBJECT(
                'scoring', JSON_OBJECT(
                'resultMode', 'max_score',
                'scoringVersion', '2.0',
                'dimensions', JSON_ARRAY('R', 'Y', 'B', 'G')
                           )
        ));


-- 性格色彩 二级文案
UPDATE user_detection_type
SET extra_info = JSON_SET(
        COALESCE(NULLIF(extra_info, ''), '{}'),
        '$.resultMeanings',
        JSON_OBJECT(
                'R', JSON_OBJECT(
                'summary', '你是红色力量型：目标导向、执行果断、抗压强。',
                'strengths', '决策快、推进力强、结果意识突出。',
                'risks', '可能过于强势，忽视过程与感受。',
                'suggestedRoles', '攻坚项目、业务管理、增长推进、创业核心岗。',
                'communicationTips', '多用“先听后定”的节奏，提升团队协同。'
                     ),
                'Y', JSON_OBJECT(
                        'summary', '你是黄色活泼型：乐观外向、表达力强、感染力高。',
                        'strengths', '沟通连接、氛围带动、机会创造能力强。',
                        'risks', '可能分散注意力，稳定性不足。',
                        'suggestedRoles', '市场传播、商务拓展、社群运营、客户经营。',
                        'communicationTips', '用目标清单约束节奏，防止“高开低走”。'
                     ),
                'B', JSON_OBJECT(
                        'summary', '你是蓝色完美型：理性严谨、注重标准、追求质量。',
                        'strengths', '分析深入、质量把控、风险意识强。',
                        'risks', '可能过度谨慎，影响推进速度。',
                        'suggestedRoles', '数据分析、质量管理、风控合规、研发测试。',
                        'communicationTips', '先给结论和优先级，再展开细节。'
                     ),
                'G', JSON_OBJECT(
                        'summary', '你是绿色和平型：稳定耐心、包容协作、重长期关系。',
                        'strengths', '团队稳定器、协同耐心、执行持续性强。',
                        'risks', '可能回避冲突，关键时刻不够果断。',
                        'suggestedRoles', '项目协调、客户成功、交付支持、组织运营。',
                        'communicationTips', '关键节点明确立场，避免过度让步。'
                     )
        )
                 )
WHERE type_code = 'COLOR4_INTL_V1';

INSERT INTO user_detection_question
(modifier, type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES ('system', 'COLOR4_INTL_V1', 'CST001', '在团队中你更像：', 1, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '目标推动者', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '气氛带动者', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '质量把关者', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '关系协调者', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST002', '面对紧急任务你通常：', 2, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '直接拍板推进', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '激励大家冲刺', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '先核对风险再做', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '先协调节奏避免失序', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST003', '你更容易被评价为：', 3, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '果断强势', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '热情开朗', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '严谨理性', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '温和稳重', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST004', '你最重视的工作价值：', 4, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '结果与效率', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '影响力与表达', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '专业与准确', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '稳定与协作', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST005', '冲突中你更可能：', 5, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '正面解决并定责', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先缓和再推进', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '依据规则与事实', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '尽量避免升级', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST006', '做决定时你更偏向：', 6, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '快决策快执行', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '看机会与场域', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '先论证再决策', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '兼顾各方感受', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST007', '你更喜欢的沟通风格：', 7, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '直接简洁', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '活跃互动', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '结构严密', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '耐心平和', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST008', '你更愿意承担的任务：', 8, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '高压攻坚', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '外联展示', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '复杂分析', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '流程维护', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST009', '被催促时你通常：', 9, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '更快推进', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先沟通预期', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '解释质量边界', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '尽量配合稳定输出', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST010', '你在陌生场景里：', 10, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '迅速掌控局面', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '主动与人连接', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '先观察后表达', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '寻找安全熟悉感', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),

       ('system', 'COLOR4_INTL_V1', 'CST011', '你更容易产生满足感于：', 11, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '达成关键目标', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '被认可与喜欢', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '做对且做精', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '关系长期稳定', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST012', '当流程低效时你会：', 12, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '重构流程提速', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先争取大家认同', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '逐项验证优化点', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '小步改进减冲击', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST013', '面对批评你更可能：', 13, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '反驳并说明立场', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先化解气氛', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '追问证据和逻辑', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '先接纳再协调', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST014', '你更常扮演的领导方式：', 14, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '目标驱动型', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '激励感染型', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '标准规范型', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '支持陪伴型', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST015', '你处理压力更常见：', 15, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '加速行动', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '寻求互动释放', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '反复检查细节', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '保持低波动节奏', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST016', '你更信任的合作伙伴是：', 16, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '有担当能拿结果', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '积极外向好协作', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '严谨可靠少失误', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '稳定耐心可长期', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST017', '当意见分歧时你更倾向：', 17, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '强势推动统一', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '通过沟通达成一致', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '依据事实裁决', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '寻求各方都可接受', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST018', '你更偏好的会议风格：', 18, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '聚焦结论与行动', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '互动充分、创意开放', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '议程严密、记录清晰', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '人人有表达空间', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST019', '你最不喜欢的状态是：', 19, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '拖延低效', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '气氛沉闷', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '粗糙混乱', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '关系紧张', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST020', '面对新任务你会先：', 20, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '确定目标与时限', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '联络资源与人脉', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '明确标准与边界', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '确认分工与协同', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),

       ('system', 'COLOR4_INTL_V1', 'CST021', '你表达观点时更像：', 21, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '直接有力', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '生动有趣', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '条理分明', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '温和克制', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST022', '你更偏好的反馈方式：', 22, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '直说问题与结果', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '轻松互动式反馈', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '基于数据和事实', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '先肯定再建议', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST023', '你最容易被哪类工作吸引：', 23, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '攻坚、竞争、突破', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '表达、互动、传播', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '分析、研究、优化', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '支持、协调、维护', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST024', '在跨部门协作里你常：', 24, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '盯结果与交付节点', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先建立连接再推动', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '定义规范接口', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '持续跟进关系稳定', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST025', '你处理变化的方式更像：', 25, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '快速切换并推进', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '把变化变成机会', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '先评估再调整', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '循序渐进地过渡', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST026', '你更愿意被如何管理：', 26, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '目标明确，充分授权', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '氛围积极，鼓励创新', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '规则清晰，标准一致', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '关心个体，稳定支持', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST027', '你在复盘中最关注：', 27, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '目标达成率', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '团队士气与协同', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '过程数据与根因', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '机制是否可持续', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST028', '你更常见的潜在短板是：', 28, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '过于强势急促', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '过于感性分散', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '过于谨慎苛求', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '过于回避冲突', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST029', '你更擅长影响他人的方式：', 29, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '目标压强与决断力', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '感染力与表达力', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '专业度与可信度', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '信任感与稳定性', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST030', '你对规则的态度：', 30, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '规则应服务结果', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '规则应保留灵活性', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '规则就是质量底线', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '规则保障公平稳定', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),

       ('system', 'COLOR4_INTL_V1', 'CST031', '你通常如何安排时间：', 31, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '优先最关键结果', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '先处理高互动事项', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '按计划清单推进', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '保持均衡不过载', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST032', '你对风险的态度更像：', 32, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '可控风险值得搏', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '有机会就可尝试', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '先控制再行动', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '尽量保持稳态', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST033', '你处理重复工作时：', 33, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '想办法提速自动化', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '加入变化保持兴趣', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '持续优化准确率', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '稳定节奏持续输出', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST034', '你更在意的回报是：', 34, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '权责与影响力', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '认可与连接', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '专业成长与品质', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '安全感与长期关系', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST035', '你更愿意跟随哪类项目：', 35, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '高目标强驱动', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '高曝光强协同', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '高复杂强专业', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '高稳定强持续', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST036', '别人最常因你受益于：', 36, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '你能拍板推进', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '你能带动气氛', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '你能保证质量', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '你能稳定协作', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST037', '你做公开表达更常：', 37, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '结论先行、立场鲜明', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '互动活跃、感染明显', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '逻辑严谨、证据充分', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '语气平和、照顾感受', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST038', '你更认同的承诺观：', 38, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '承诺就必须达成', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '承诺也要有体验感', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '承诺需可验证可追踪', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '承诺意味着长期可靠', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST039', '你更习惯的说服方式：', 39, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '强调目标收益', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '用场景故事打动', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '用数据逻辑论证', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '先建立信任再推进', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL),
       ('system', 'COLOR4_INTL_V1', 'CST040', '总体上你最接近：', 40, '请选择最符合你的一项',
        JSON_ARRAY(
                JSON_OBJECT('key', 'A', 'text', '结果导向的行动者', 'dimension', 'R', 'scores', JSON_OBJECT('R', 1)),
                JSON_OBJECT('key', 'B', 'text', '乐观外向的连接者', 'dimension', 'Y', 'scores', JSON_OBJECT('Y', 1)),
                JSON_OBJECT('key', 'C', 'text', '理性严谨的思考者', 'dimension', 'B', 'scores', JSON_OBJECT('B', 1)),
                JSON_OBJECT('key', 'D', 'text', '温和稳定的支持者', 'dimension', 'G', 'scores', JSON_OBJECT('G', 1))
        ), NULL);