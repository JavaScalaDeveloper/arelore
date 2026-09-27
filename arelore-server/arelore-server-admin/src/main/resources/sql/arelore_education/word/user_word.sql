USE arelore_education;

# 背单词用户侧业务表（与考证 user_detection_* 一样落 education 库）。
# 前缀用 user_word_：按 user_id 读写，与 admin_word_* 主数据分离。

# drop table if exists `user_word_learn_record`;
# drop table if exists `user_word_current_book`;

# 用户当前使用的单词本，一人一本当前词书
CREATE TABLE `user_word_current_book`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime             default current_timestamp not null comment '创建时间',
    `modify_time` datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`     decimal(20, 0) not null comment '用户业务ID',
    `book_code`   varchar(64)    not null default '' comment '当前单词本code',
    `learn_done`  int            not null default 0 comment '新学已完成数',
    `learn_todo`  int            not null default 0 comment '新学待完成数',
    `review_done` int            not null default 0 comment '复习已完成数',
    `review_todo` int            not null default 0 comment '复习待完成数',
    `ext_info`    text                   default null comment '当前进度拓展(JSON)，如游标、每日配额',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id` (`user_id`),
    key `idx_book_code` (`book_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户当前单词本';

# 用户背单词记录：同一用户+词本+单词一条，记忆结论多版本放 text
CREATE TABLE `user_word_learn_record`
(
    `id`             bigint auto_increment comment '主键ID',
    `create_time`    datetime             default current_timestamp not null comment '创建时间',
    `modify_time`    datetime             default current_timestamp not null on update current_timestamp comment '修改时间',
    `user_id`        decimal(20, 0) not null comment '用户业务ID',
    `book_code`      varchar(64)    not null default '' comment '单词本code',
    `word_code`      varchar(128)   not null default '' comment '单词code',
    `ext_info`       text                   default null comment '拓展信息(JSON)：word、rememberFlag、learnCount、reviewCount、lastLearnTime、记忆多版本记录等',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_user_id_book_code_word_code` (`user_id`, `book_code`, `word_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户背单词记录';
