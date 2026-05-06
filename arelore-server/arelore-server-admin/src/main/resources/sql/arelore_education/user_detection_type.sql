-- =============================================
-- MBTI 性格测试数据库结构
-- =============================================
use arelore_education;
# drop table if exists `user_detection_type`;
# drop table if exists `user_detection_question`;
-- 1. 检测类型表
CREATE TABLE `user_detection_type`
(
    `id`               bigint auto_increment comment '主键ID',
    `create_time`      datetime     default current_timestamp not null comment '创建时间',
    `modify_time`      datetime     default current_timestamp not null on update current_timestamp comment '修改时间',
    modifier           varchar(64)  default ''                not null comment '修改人',
    `type_code`        varchar(64)                            not null default '' comment '检测类型Code',
    `type_name`        varchar(255)                           not null default '' comment '检测类型名',
    `type_description` varchar(255) default '' comment '检测类型描述',
    `extra_info`       text         default null comment '拓展信息(JSON格式)',
    primary key (`id`),
    unique key `uk_type_code` (`type_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户检测类型表';

-- 2. 题目表
CREATE TABLE `user_detection_question`
(
    `id`                   bigint auto_increment comment '主键ID',
    `create_time`          datetime              default current_timestamp not null comment '创建时间',
    `modify_time`          datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    modifier               varchar(64)           default '' not null comment '修改人',
    `type_code`            varchar(64)  not null default '' comment '检测类型Code',
    `question_code`        varchar(64)  not null default '' comment '题目Code',
    `question_name`        varchar(255) not null default '' comment '题目名称',
    `question_order`       int          not null default 0 comment '题目排序编号',
    `question_description` varchar(255)          default '' comment '题目描述',
    `options`              text                  default null comment '选项',
    `extra_info`           text                  default null comment '拓展信息(JSON格式)',
    primary key (`id`),
    unique key `uk_type_question_code` (`type_code`, `question_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户检测题目表';
