USE arelore_education;

-- 用户题目收藏表（最小字段设计）
CREATE TABLE IF NOT EXISTS `user_detection_question_favorite`
(
    `id`                 bigint auto_increment comment '主键ID',
    `create_time`        datetime    default current_timestamp not null comment '创建时间',
    `user_id`            varchar(64) default ''                not null comment '用户ID',
    `question_type_code` varchar(64) default ''                not null comment '题目类型编码',
    `question_code`      varchar(64) default ''                not null comment '题目编码',
    `extra_info`         text                                   null comment '拓展信息(JSON)',
    primary key (`id`),
    unique key `uk_user_type_question` (`user_id`, `question_type_code`, `question_code`),
    key `idx_user_id` (`user_id`),
    key `idx_type_code` (`question_type_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户题目收藏表';
