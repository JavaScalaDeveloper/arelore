USE arelore_user;

drop table if exists `user_auth_session`;

# 登录会话：只存 token + 过期时间 + 用户展示 JSON；校验走 uk_token，登出直接删行
CREATE TABLE `user_auth_session`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime               default current_timestamp not null comment '创建时间',
    `modify_time` datetime               default current_timestamp not null on update current_timestamp comment '修改时间',
    `token`       varchar(128)  not null comment '登录令牌（与 Authorization 头一致）',
    `expire_time` datetime      not null comment '过期时间',
    `user_info`   varchar(512)  not null comment '用户展示信息 JSON（id/username/nickname/avatar）',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_token` (`token`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户登录会话表';
