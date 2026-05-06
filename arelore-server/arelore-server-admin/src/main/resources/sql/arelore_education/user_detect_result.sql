USE arelore_education;

-- 当前检测结果表（每个用户每种检测类型仅保留一条当前结果）
# DROP TABLE IF EXISTS `user_detect_result`;
CREATE TABLE IF NOT EXISTS `user_detect_result`
(
    `id`                    bigint auto_increment comment '主键ID',
    `create_time`           datetime              default current_timestamp not null comment '创建时间',
    `modify_time`           datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`               varchar(64)  not null default '' comment '用户ID',
    `user_detect_type_code` varchar(64)  not null default '' comment '检测类型编码',
    `user_detect_result`    varchar(255) not null default '' comment '检测结果',
    `extra_info`            text                  default null comment '拓展信息(JSON，含每题标题与选项内容等)',
    primary key (`id`),
    unique key `uk_type_user` (`user_detect_type_code`, `user_id`),
    key `idx_user_id` (`user_id`),
    key `idx_type_code` (`user_detect_type_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户当前检测结果表';

-- 历史检测结果表（保留每次版本记录，不做唯一约束）
# DROP TABLE IF EXISTS `user_detect_result_history`;
CREATE TABLE IF NOT EXISTS `user_detect_result_history`
(
    `id`                    bigint auto_increment comment '主键ID',
    `create_time`           datetime              default current_timestamp not null comment '创建时间',
    `modify_time`           datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`               varchar(64)  not null default '' comment '用户ID',
    `user_detect_type_code` varchar(64)  not null default '' comment '检测类型编码',
    `user_detect_result`    varchar(255) not null default '' comment '检测结果',
    `extra_info`            text                  default null comment '拓展信息(JSON，含每题标题与选项内容等)',
    primary key (`id`),
    key `idx_user_id` (`user_id`),
    key `idx_type_code` (`user_detect_type_code`),
    key `idx_create_time` (`create_time`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户历史检测结果表';