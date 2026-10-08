USE arelore_education;

# drop table if exists `admin_word_base_info`;

# 单词基础信息表：按「大小写敏感」的词形唯一；供 admin_word_entry.word 关联取图等
# 定时/按需从有道 jsonapi（或其它源）拉取后写入；勿把完整 jsonapi 大包原样落库
# source / last_sync_time 等放在 ext_info
CREATE TABLE `admin_word_base_info`
(
    `id`            bigint auto_increment comment '主键ID',
    `create_time`   datetime              default current_timestamp not null comment '创建时间',
    `modify_time`   datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `language_code` varchar(32)  not null default 'EN' comment '语种code，与 admin_word_language.code 对齐',
    `word`          varchar(128) collate utf8mb4_bin not null comment '词形原文，大小写敏感（us 与 US 不同行）',
    `ext_info`      text                  default null comment '拓展信息(JSON)：pictures[{url}], source, lastSyncTime, ukphone/usphone 等；勿存完整 jsonapi',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_language_code_word` (`language_code`, `word`),
    key `idx_word` (`word`)
) engine = InnoDB
  default charset = utf8mb4 comment ='单词基础信息（大小写敏感，缓存配图等）';

# 关联约定（应用层，不强制 FK）：
#   admin_word_entry.word = admin_word_base_info.word
#   且 language_code 与词本所属语种一致（当前多为 EN）
# 查询示例：
#   select e.*, b.ext_info as base_ext
#   from admin_word_entry e
#   left join admin_word_base_info b
#     on b.language_code = 'EN' and b.word = e.word
#   where e.book_code = ?;
