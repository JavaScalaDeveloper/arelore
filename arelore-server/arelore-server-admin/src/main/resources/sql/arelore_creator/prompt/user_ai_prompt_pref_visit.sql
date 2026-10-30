USE arelore_creator;

-- ============================================================================
-- AI 提示词 App：用户端偏好 + 日访问（PV/UV）
-- - pref：一人一行；按 user_id / status 查；字体、复制字段等不进条件，全部进 ext_json
-- - daily_visit：一人一日一端一行；管理端按日汇总 PV / UV
-- - 用户配置过的提示词继续查既有 user_ai_prompt（按 user_id）
-- ============================================================================

-- drop table if exists `user_ai_prompt_daily_visit`;
-- drop table if exists `user_ai_prompt_pref`;

-- ---------------------------------------------------------------------------
-- 用户端偏好（个人中心）
-- 可查询列：user_id、status、时间戳
-- ext_json 约定（读写整包，不做 JSON 条件检索）：
-- {
--   "fontScale": 1.00,          -- 0.85小 / 1.00标准 / 1.15大 / 1.30更大
--   "fontSizeSp": 14,           -- 列表基准 sp，建议 12~20
--   "copyFields": ["PROMPT_TEXT"],
--       -- 顺序即拼接顺序，合法值：
--       -- TITLE / SUMMARY / PROMPT_TEXT / PUBLISH_STATUS /
--       -- CONFIG_TAGS / MANUAL_TAGS / PROMPT_UUID
--   "copySep": "\n",            -- 字段分隔符；"\n\n" 表示空行
--   "clientVersion": "1.0.0"    -- 可选
-- }
-- status：NORMAL-正常 DELETED-已删除(软删)
-- ---------------------------------------------------------------------------
CREATE TABLE `user_ai_prompt_pref`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime             default current_timestamp not null comment '创建时间',
    `modify_time` datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`     decimal(20, 0) not null comment '所属用户业务ID',
    `status`      varchar(32)    not null default 'NORMAL' comment '数据状态: NORMAL-正常 DELETED-已删除(软删)',
    `ext_json`    json                    default null comment '偏好整包(JSON)：fontScale/fontSizeSp/copyFields/copySep 等，不做条件检索',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id` (`user_id`),
    key `idx_status_modify_time` (`status`, `modify_time`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户AI提示词客户端偏好（按user_id查，细节在ext_json）';

-- ---------------------------------------------------------------------------
-- 提示词 App 日访问（产品维度 PV/UV）
-- 上报：INSERT ... ON DUPLICATE KEY UPDATE visit_count = visit_count + 1
-- 日 PV = SUM(visit_count)；日 UV = COUNT(DISTINCT user_id)（有行即算 1 UV）
-- client：ANDROID / WEB / MINI / UNKNOWN
-- ---------------------------------------------------------------------------
CREATE TABLE `user_ai_prompt_daily_visit`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime             default current_timestamp not null comment '创建时间',
    `modify_time` datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`     decimal(20, 0) not null comment '用户业务ID',
    `visit_date`  date           not null comment '访问自然日（Asia/Shanghai）',
    `visit_count` int            not null default 0 comment '当日访问次数（PV 累加）',
    `client`      varchar(32)    not null default 'ANDROID' comment '端: ANDROID/WEB/MINI/UNKNOWN',
    `ext_json`    json                    default null comment '拓展信息(JSON)：末次UA/渠道/版本等，不做条件检索',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id_visit_date_client` (`user_id`, `visit_date`, `client`),
    key `idx_visit_date` (`visit_date`),
    key `idx_visit_date_client` (`visit_date`, `client`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户AI提示词App日访问（产品PV/UV）';

-- 若已建表缺 create_time，执行：
-- ALTER TABLE `user_ai_prompt_daily_visit`
--   ADD COLUMN `create_time` datetime default current_timestamp not null comment '创建时间' AFTER `id`,
--   ADD KEY `idx_create_time` (`create_time`);
