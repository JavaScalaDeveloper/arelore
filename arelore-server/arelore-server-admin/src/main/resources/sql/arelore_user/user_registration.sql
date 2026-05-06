USE arelore_user;
# drop table if exists `user_registration_application`;

# 用户注册申请表，记录用户每次提交注册申请的记录，不一定成功
CREATE TABLE `user_registration_application`
(
    `id`           bigint auto_increment comment '主键ID',
    `create_time`  datetime              default current_timestamp not null comment '创建时间',
    `modify_time`  datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `account_type` varchar(32)  not null default '' comment '账号类型: MOBILE, WECHAT, EMAIL等等',
    `account`      varchar(128) not null default '' comment '账号',
    `client_ip`    varchar(64)  not null default '' comment '客户端IP',
    `ext_info`     text                  default null comment '拓展信息(JSON)',
    primary key (`id`),
    key `idx_account` (`account_type`, `account`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户注册申请表';

# drop table if exists `user_registration_result`;
# 用户注册结果表，记录用户注册成功的信息
CREATE TABLE `user_registration_result`
(
    `id`                bigint auto_increment comment '主键ID',
    `create_time`       datetime                default current_timestamp not null comment '创建时间',
    `modify_time`       datetime                default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`           DECIMAL(20, 0) NOT NULL COMMENT '用户业务ID (格式: 2688 + 16位随机数，排除数字4)',
    `merged_to_user_id` decimal(20, 0)          default null comment '合并指向的user_id，空表示未合并，例如账号A、绑定了B，那么B指向A，C绑定了B或A，那么C也指向A',
    `account_type`      varchar(32)    not null default '' comment '账号类型: MOBILE, WECHAT, EMAIL等等',
    `account`           varchar(128)   not null default '' comment '账号',
    `status`            tinyint        not null default 0 comment '账号状态: 1-正常, 2-封禁, 3-停用，注销后归档，防止新用户注册看到上一任用户的信息',
    `ext_info`          text                    default null comment '拓展信息(JSON)',
    primary key (`id`),
    unique key `uk_user_id` (`user_id`),
    unique key `uk_account_type_account` (`account_type`, `account`),
    key `idx_merged_to_user_id` (`merged_to_user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户注册结果表';


select *
from user_registration_result;
