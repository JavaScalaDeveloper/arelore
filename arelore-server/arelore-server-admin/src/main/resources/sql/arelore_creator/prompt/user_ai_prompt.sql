USE arelore_creator;

-- ============================================================================
-- 【个人维度】AI 提示词（MySQL 8）
-- - 数据归属 user_id，PC / 手机同一账号读写
-- - 每人每天新增上限 100 条（业务层按 user_id + 当日 create_time 计数）
-- - summary / importBatchId 等放 ext_info
-- - prompt_uuid：Java 生成 UUID 去横线（32 位），导入按 (user_id, prompt_uuid) 覆盖
-- - 两套标签（JSON 数组 + 多值索引，支持按标签精确命中走索引）：
--   config_tags：配置标签（可手打，也可被 CSV 导入覆盖）
--   manual_tags：人工标签（仅页面修改，导入不碰）
-- - 标签查询示例（可走 idx_*_tags 多值索引）：
--     WHERE '抖音' MEMBER OF (config_tags)
--     WHERE JSON_CONTAINS(manual_tags, JSON_QUOTE('英语'))
--     WHERE ('b' MEMBER OF (config_tags) OR 'b' MEMBER OF (manual_tags)
--        OR  'c' MEMBER OF (config_tags) OR 'c' MEMBER OF (manual_tags))
-- ============================================================================

-- ---------------------------------------------------------------------------
-- AI 提示词（个人）
-- config_tags / manual_tags：JSON 数组，默认 []；勿再用 text+LIKE（无法有效利用索引）
-- publish_status：DRAFT-草稿 PRODUCING-制作中 PUBLISHED-已发布 OFFLINE-已下架
-- status：NORMAL-正常 DELETED-已删除(软删)
-- ---------------------------------------------------------------------------
drop table if exists `user_ai_prompt`;
drop table if exists `user_ai_prompt_tag`;

CREATE TABLE `user_ai_prompt`
(
    `id`             bigint auto_increment comment '主键ID',
    `create_time`    datetime             default current_timestamp not null comment '创建时间',
    `modify_time`    datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`        decimal(20, 0) not null comment '所属用户业务ID',
    `prompt_uuid`    varchar(32)    not null comment '业务UUID（去横线，Java生成；导入按本字段覆盖）',
    `title`          varchar(255)   not null default '' comment '标题',
    `prompt_text`    mediumtext     not null comment '提示词正文（应用层限长 65535 字符；手机默认复制本字段）',
    `config_tags`    json           not null default (json_array()) comment '配置标签(JSON字符串数组)，可手打也可被CSV导入覆盖，如 ["小红书"] 或 ["抖音","视频号"]',
    `manual_tags`    json           not null default (json_array()) comment '人工标签(JSON字符串数组)，仅页面修改，导入不覆盖',
    `publish_status` varchar(32)    not null default 'DRAFT' comment '发布状态: DRAFT-草稿 PRODUCING-制作中 PUBLISHED-已发布 OFFLINE-已下架',
    `status`         varchar(32)    not null default 'NORMAL' comment '数据状态: NORMAL-正常 DELETED-已删除(软删)',
    `ext_info`       json                    default null comment '拓展信息(JSON)：summary/importBatchId/copyTemplate/source 等',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id_prompt_uuid` (`user_id`, `prompt_uuid`),
    key `idx_user_id_publish_status` (`user_id`, `publish_status`),
    key `idx_user_id_status_create_time` (`user_id`, `status`, `create_time`),
    key `idx_user_id_title` (`user_id`, `title`),
    -- MySQL 8.0.17+ 多值索引：按数组元素精确匹配时可走索引（替代 text LIKE）
    key `idx_config_tags` ((cast(`config_tags` as char(64) array))),
    key `idx_manual_tags` ((cast(`manual_tags` as char(64) array)))
) engine = InnoDB
  default charset = utf8mb4 comment ='用户AI提示词（个人维度）';

-- ---------------------------------------------------------------------------
-- 用户标签字典（个人）
-- tag_type：CONFIG-配置标签 MANUAL-人工标签
-- status：NORMAL-启用 DISABLED-停用 DELETED-已删除(软删)
-- ---------------------------------------------------------------------------
CREATE TABLE `user_ai_prompt_tag`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime             default current_timestamp not null comment '创建时间',
    `modify_time` datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`     decimal(20, 0) not null comment '所属用户业务ID',
    `tag_type`    varchar(32)    not null default 'MANUAL' comment '标签类型: CONFIG-配置标签 MANUAL-人工标签',
    `name`        varchar(64)    not null default '' comment '标签名',
    `color`       varchar(32)    not null default '' comment '展示色值，空串表示默认',
    `sort_order`  int            not null default 0 comment '排序，越小越靠前',
    `status`      varchar(32)    not null default 'NORMAL' comment '状态: NORMAL-启用 DISABLED-停用 DELETED-已删除(软删)',
    `ext_info`    json                    default null comment '拓展信息(JSON)',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id_tag_type_name` (`user_id`, `tag_type`, `name`),
    key `idx_user_id_tag_type_status_sort` (`user_id`, `tag_type`, `status`, `sort_order`),
    key `idx_user_id_name` (`user_id`, `name`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户AI提示词标签字典（个人维度）';
