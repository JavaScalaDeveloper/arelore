USE arelore_common;

# C 端用户日访问统计：同一用户同一自然日一行，visit_count 累加。
# 上报用 INSERT ... ON DUPLICATE KEY UPDATE visit_count = visit_count + 1。
# 管理端「公共基础」菜单查询/汇总本表。

# drop table if exists `common_user_daily_visit`;

CREATE TABLE `common_user_daily_visit`
(
    `id`          bigint auto_increment comment '主键ID',
    `modify_time` datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`     decimal(20, 0) not null comment '用户业务ID（与 user_registration_result.user_id 一致）',
    `visit_date`  date           not null comment '访问自然日（服务端时区，建议 Asia/Shanghai）',
    `visit_count` int            not null default 0 comment '当日访问次数',
    `ext_info`    text                   default null comment '拓展信息(JSON)，如 client/渠道/末次 UA 等',
    primary key (`id`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id_visit_date` (`user_id`, `visit_date`),
    key `idx_visit_date` (`visit_date`)
) engine = InnoDB
  default charset = utf8mb4 comment ='C端用户日访问量统计';
