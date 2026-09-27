USE arelore_education;

-- 如需重置
DELETE
FROM user_detection_question
WHERE type_code = 'MBTI';
DELETE
FROM user_detection_type
WHERE type_code = 'MBTI';

INSERT INTO user_detection_type
    (modifier, type_code, type_name, type_description, extra_info)
VALUES ('system',
        'MBTI',
        'MBTI 性格测试（标准40题）',
        '基于 E/I、S/N、T/F、J/P 四组维度对决的标准化人格测试',
        JSON_OBJECT(
                'scoring', JSON_OBJECT(
                'resultMode', 'pair_compare',
                'scoringVersion', '2.0',
                'tieBreaker', 'left',
                'pairs', JSON_ARRAY(
                        JSON_OBJECT('left', 'E', 'right', 'I'),
                        JSON_OBJECT('left', 'S', 'right', 'N'),
                        JSON_OBJECT('left', 'T', 'right', 'F'),
                        JSON_OBJECT('left', 'J', 'right', 'P')
                         )
                           )
        ));

-- MBTI 二级文案
UPDATE user_detection_type
SET extra_info = JSON_SET(
        COALESCE(NULLIF(extra_info, ''), '{}'),
        '$.resultMeanings',
        JSON_OBJECT(
                'INTJ', JSON_OBJECT(
                'summary','你是战略型思考者：独立、前瞻、目标感强。',
                'strengths','系统规划、长期布局、复杂问题拆解能力强。',
                'risks','可能显得过于苛刻或沟通温度不足。',
                'suggestedRoles','架构设计、战略规划、产品策略、技术管理。',
                'communicationTips','先讲目标与逻辑，再补充对人的关注与反馈。'
                        ),
                'INTP', JSON_OBJECT(
                        'summary','你是逻辑探索者：好奇、理性、重原理。',
                        'strengths','抽象建模、分析推理、创新解法能力突出。',
                        'risks','可能陷入过度思考，推进节奏偏慢。',
                        'suggestedRoles','算法研发、技术研究、数据分析、咨询分析。',
                        'communicationTips','先给结论再展开推理，提升协作效率。'
                        ),
                'ENTJ', JSON_OBJECT(
                        'summary','你是指挥型领导者：果断高效，擅长推动结果。',
                        'strengths','资源整合、决策执行、组织推进能力强。',
                        'risks','可能忽视情绪管理，给团队带来压迫感。',
                        'suggestedRoles','业务负责人、项目总控、运营管理、创业管理。',
                        'communicationTips','决策时加入倾听环节，提升团队认同。'
                        ),
                'ENTP', JSON_OBJECT(
                        'summary','你是创新型辩证者：灵活机敏，点子多。',
                        'strengths','机会识别、跨界联想、破局能力强。',
                        'risks','可能兴趣转移快，收尾与细节不足。',
                        'suggestedRoles','创新产品、增长策略、商业拓展、咨询顾问。',
                        'communicationTips','明确优先级与落地责任，避免“只发散不收敛”。'
                        ),
                'INFJ', JSON_OBJECT(
                        'summary','你是洞察型引导者：有同理心且重价值。',
                        'strengths','深度观察、长期影响、人际引导能力强。',
                        'risks','容易内耗，对冲突较敏感。',
                        'suggestedRoles','组织发展、用户研究、心理咨询、教育培训。',
                        'communicationTips','表达观点时更直接，减少“对方自行体会”的成本。'
                        ),
                'INFP', JSON_OBJECT(
                        'summary','你是理想型共情者：真诚、敏感、重意义。',
                        'strengths','创意表达、价值共鸣、内容创造能力好。',
                        'risks','遇到高压目标时可能回避冲突。',
                        'suggestedRoles','内容创作、品牌传播、体验设计、公益教育。',
                        'communicationTips','把价值诉求转成可执行步骤，增强协同。'
                        ),
                'ENFJ', JSON_OBJECT(
                        'summary','你是鼓舞型组织者：外向温暖，善于凝聚团队。',
                        'strengths','激励他人、建立共识、推动协作效果好。',
                        'risks','可能过度承担他人情绪与责任。',
                        'suggestedRoles','团队管理、客户成功、培训发展、人力BP。',
                        'communicationTips','适度设边界，先保证目标与节奏。'
                        ),
                'ENFP', JSON_OBJECT(
                        'summary','你是热情型连接者：乐观开放，感染力强。',
                        'strengths','关系建立快、创意表达强、适应变化快。',
                        'risks','可能分心于新鲜事，持续性不足。',
                        'suggestedRoles','市场传播、社区运营、商务拓展、创意策划。',
                        'communicationTips','用看板或节奏机制保障持续交付。'
                        ),
                'ISTJ', JSON_OBJECT(
                        'summary','你是执行型守护者：稳健负责，重规则与秩序。',
                        'strengths','流程管理、风险控制、交付稳定性强。',
                        'risks','可能对变化不够灵活。',
                        'suggestedRoles','项目管理、质量管理、财务审计、运维治理。',
                        'communicationTips','在坚持标准同时，预留试错窗口。'
                        ),
                'ISFJ', JSON_OBJECT(
                        'summary','你是支持型保障者：耐心可靠，重承诺。',
                        'strengths','服务意识强、细节周全、团队支持稳定。',
                        'risks','容易过度照顾他人忽视自身负荷。',
                        'suggestedRoles','客户服务、行政支持、交付协调、医疗护理。',
                        'communicationTips','明确优先级与边界，避免“默默超负荷”。'
                        ),
                'ESTJ', JSON_OBJECT(
                        'summary','你是管理型推进者：务实高效，标准清晰。',
                        'strengths','组织执行、制度落地、目标达成能力强。',
                        'risks','可能过于强调规则，压制创新。',
                        'suggestedRoles','运营管理、交付管理、组织管理、供应链管理。',
                        'communicationTips','在规则之外保留创新讨论空间。'
                        ),
                'ESFJ', JSON_OBJECT(
                        'summary','你是协作型协调者：亲和周到，重关系质量。',
                        'strengths','协作润滑、服务导向、团队稳定器。',
                        'risks','可能因求和谐而延迟必要决策。',
                        'suggestedRoles','客户关系、团队协调、教育服务、社群运营。',
                        'communicationTips','遇关键问题要及时拍板，避免拖延。'
                        ),
                'ISTP', JSON_OBJECT(
                        'summary','你是实干型问题解决者：冷静务实，动手能力强。',
                        'strengths','排障能力强、临场应对快、执行效率高。',
                        'risks','可能不爱解释过程，协作透明度不足。',
                        'suggestedRoles','工程实施、运维排障、硬件开发、应急支持。',
                        'communicationTips','补充过程同步，减少团队信息差。'
                        ),
                'ISFP', JSON_OBJECT(
                        'summary','你是体验型创作者：温和细腻，审美与感知好。',
                        'strengths','用户体验敏感、表达自然、创造力稳定。',
                        'risks','在高冲突场景中可能回避表达。',
                        'suggestedRoles','视觉设计、交互体验、内容创意、品牌设计。',
                        'communicationTips','提前准备表达框架，提升立场清晰度。'
                        ),
                'ESTP', JSON_OBJECT(
                        'summary','你是行动型开拓者：反应快、胆识足、结果导向。',
                        'strengths','机会把握、谈判推进、现场决策能力强。',
                        'risks','可能低估长期风险与复盘。',
                        'suggestedRoles','销售拓展、增长运营、项目攻坚、业务开拓。',
                        'communicationTips','关键动作后补齐复盘与风控。'
                        ),
                'ESFP', JSON_OBJECT(
                        'summary','你是活力型表现者：外向热情，互动感强。',
                        'strengths','现场带动、关系经营、用户沟通能力好。',
                        'risks','可能在深度分析与长期规划上投入不足。',
                        'suggestedRoles','活动运营、主播讲解、客户经营、品牌传播。',
                        'communicationTips','与“规划型伙伴”搭配，形成互补。'
                        )
        )
                 )
WHERE type_code = 'MBTI';



INSERT INTO user_detection_question
(modifier, type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
-- E/I (1-10)
('system', 'MBTI', 'MBTI_Q001', '在社交场合中你通常：', 1, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '主动与多人交流', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '更愿意与少数熟人深聊', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q002', '周末结束后你更常感觉：', 2, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '与人互动后更有活力', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '独处后更能恢复精力', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q003', '开会时你通常会：', 3, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '边想边说，先表达再调整', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '先思考成熟再发言', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q004', '结识新朋友时你更倾向：', 4, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '主动破冰、快速熟络', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '慢热观察、逐步建立信任', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q005', '遇到问题你更常：', 5, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '找人讨论激发思路', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '自己沉淀形成判断', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q006', '在团队里你更自然的角色：', 6, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '外向联络者', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '内省思考者', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q007', '你更喜欢的工作环境：', 7, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '开放协作、频繁互动', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '相对安静、减少打扰', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q008', '做重要决定前你更倾向：', 8, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '先和人沟通想法', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '先独立推演再沟通', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q009', '参加大型活动时你通常：', 9, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '积极参与并结识新朋友', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '阶段性离场充电', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q010', '你更容易被他人评价为：', 10, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '外向健谈', 'dimension', 'E', 'scores', JSON_OBJECT('E', 1)),
         JSON_OBJECT('key', 'B', 'text', '沉稳内敛', 'dimension', 'I', 'scores', JSON_OBJECT('I', 1))
 ), NULL),

-- S/N (11-20)
('system', 'MBTI', 'MBTI_Q011', '获取信息时你更关注：', 11, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '事实细节与当前情况', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '趋势可能与未来图景', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q012', '你更信任：', 12, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '经验和可验证证据', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '直觉和潜在关联', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q013', '阅读说明时你更偏好：', 13, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '步骤明确、操作具体', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '先看整体概念和原理', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q014', '讨论方案时你更常提出：', 14, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '执行细节和落地条件', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '创新方向和延展可能', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q015', '你更喜欢的学习方式：', 15, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '从具体案例逐步掌握', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '从抽象模型快速迁移', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q016', '你更容易注意到：', 16, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '现实中的异常细节', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '事物背后的模式', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q017', '当计划变更时你更先考虑：', 17, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '现有资源和实际影响', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '长期机会和潜在价值', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q018', '你描述事物时更常：', 18, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '按时间线和事实复述', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '按主题和意义概括', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q019', '面对新概念你更倾向：', 19, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '先看是否实用可执行', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '先看是否启发新可能', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q020', '你更容易被哪类任务吸引：', 20, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '需要耐心打磨细节的任务', 'dimension', 'S', 'scores', JSON_OBJECT('S', 1)),
         JSON_OBJECT('key', 'B', 'text', '需要构思创新方向的任务', 'dimension', 'N', 'scores', JSON_OBJECT('N', 1))
 ), NULL),

-- T/F (21-30)
('system', 'MBTI', 'MBTI_Q021', '做决定时你更优先：', 21, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '客观逻辑与一致标准', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '人的感受与价值影响', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q022', '给同事反馈时你更倾向：', 22, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '直接指出问题和改进点', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '先共情再给建议', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q023', '处理冲突时你更常：', 23, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '回到事实与规则裁决', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '先修复关系再谈分歧', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q024', '你更认可的公平是：', 24, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '对所有人统一标准', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '结合个体处境灵活处理', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q025', '评估一个方案时你更先看：', 25, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '逻辑闭环与风险可控', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '对人群的体验与接受度', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q026', '你更容易说服他人的方式：', 26, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '数据和论证', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '故事和共鸣', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q027', '在团队分工时你更看重：', 27, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '能力匹配与效率最优', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '意愿匹配与关系和谐', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q028', '当他人情绪化时你通常：', 28, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '引导回到问题本身', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '先接住情绪再处理问题', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q029', '你更常被评价为：', 29, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '理性客观', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '温暖体贴', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q030', '面对取舍你更倾向：', 30, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '选择“更正确”的方案', 'dimension', 'T', 'scores', JSON_OBJECT('T', 1)),
         JSON_OBJECT('key', 'B', 'text', '选择“更合适人”的方案', 'dimension', 'F', 'scores', JSON_OBJECT('F', 1))
 ), NULL),

-- J/P (31-40)
('system', 'MBTI', 'MBTI_Q031', '对工作计划你更偏好：', 31, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '提前规划并按节点推进', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '保持弹性视情况调整', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q032', '旅行时你更喜欢：', 32, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '行程确定、预订齐全', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '边走边看、随遇而安', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q033', '面对截止时间你通常：', 33, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '尽早完成避免积压', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '临近截止集中爆发', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q034', '你桌面的常态是：', 34, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '有序分类、随手可取', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '看似杂乱但自己有数', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q035', '当出现新机会时你更常：', 35, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '先评估对既定计划影响', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '先尝试再决定是否纳入', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q036', '你更喜欢哪种团队节奏：', 36, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '规则清晰、职责明确', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '灵活协作、快速迭代', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q037', '你通常会：', 37, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '先做决定再行动', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '先行动再逐步定型', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q038', '你处理待办事项更常：', 38, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '按优先级逐项清空', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '按状态切换并行推进', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q039', '当事情未收尾时你感觉：', 39, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '不踏实，想尽快闭环', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '可接受，保持开放更好', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL),
('system', 'MBTI', 'MBTI_Q040', '你更认同的工作方式：', 40, '请选择最符合你的一项',
 JSON_ARRAY(
         JSON_OBJECT('key', 'A', 'text', '计划驱动', 'dimension', 'J', 'scores', JSON_OBJECT('J', 1)),
         JSON_OBJECT('key', 'B', 'text', '探索驱动', 'dimension', 'P', 'scores', JSON_OBJECT('P', 1))
 ), NULL);