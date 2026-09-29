USE arelore_education;

# 背单词主数据（管理端维护、C 端只读）。
# 前缀用 admin_word_：与用户行为表 user_word_* 分开，避免和 user_detection_* 抢同一语义。

# drop table if exists `admin_word_entry`;
# drop table if exists `admin_word_book`;
# drop table if exists `admin_word_category`;
# drop table if exists `admin_word_language`;

# 语种类型，如英语、法语；当前仅预置英语
CREATE TABLE `admin_word_language`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime              default current_timestamp not null comment '创建时间',
    `modify_time` datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `code`        varchar(32)  not null default '' comment '语种code，仅大写字母/数字/_，创建后不可改，如 EN、FR',
    `name`        varchar(64)  not null default '' comment '语种名称',
    `description` varchar(255) not null default '' comment '描述',
    `status`      tinyint      not null default 1 comment '状态: 1-启用, 0-停用',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_code` (`code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='单词语种类型';

# 单词本分类：同一语种下 code 唯一；粗分学段/考试，教材版本放 book.ext_info
CREATE TABLE `admin_word_category`
(
    `id`            bigint auto_increment comment '主键ID',
    `create_time`   datetime              default current_timestamp not null comment '创建时间',
    `modify_time`   datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `language_code` varchar(32)  not null default '' comment '语种code，仅大写字母/数字/_，创建后不可改',
    `code`          varchar(32)  not null default '' comment '分类code，仅大写字母/数字/_，创建后不可改，如 JUNIOR、OVERSEAS',
    `name`          varchar(64)  not null default '' comment '分类名称',
    `description`   varchar(255) not null default '' comment '描述',
    `sort_no`       int          not null default 0 comment '排序，越小越靠前',
    `status`        tinyint      not null default 1 comment '状态: 1-启用, 0-停用',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_language_code_code` (`language_code`, `code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='单词本分类';

# 单词本
CREATE TABLE `admin_word_book`
(
    `id`            bigint auto_increment comment '主键ID',
    `create_time`   datetime              default current_timestamp not null comment '创建时间',
    `modify_time`   datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `code`          varchar(64)  not null default '' comment '单词本code，仅大写字母/数字/_，创建后不可改',
    `name`          varchar(128) not null default '' comment '单词本名称',
    `description`   varchar(255) not null default '' comment '描述',
    `word_count`    int          not null default 0 comment '词数',
    `language_code` varchar(32)  not null default '' comment '语种code，仅大写字母/数字/_，创建后不可改',
    `category_code` varchar(32)  not null default '' comment '分类code，仅大写字母/数字/_，创建后不可改',
    `status`        tinyint      not null default 1 comment '状态: 1-启用, 0-停用',
    `ext_info`      text                  default null comment '拓展信息(JSON)，如封面文件id',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    unique key `uk_code` (`code`),
    key `idx_language_code_category_code` (`language_code`, `category_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='单词本';

# 单词本词条：按 book_code / word_code / word / sort_no 查询；音标词性释义例句不进条件，放 ext_info
CREATE TABLE `admin_word_entry`
(
    `id`          bigint auto_increment comment '主键ID',
    `create_time` datetime              default current_timestamp not null comment '创建时间',
    `modify_time` datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `book_code`   varchar(64)  not null default '' comment '单词本code，仅大写字母/数字/_，创建后不可改',
    `word_code`   varchar(128) not null default '' comment '单词code，词本内唯一，仅大写字母/数字/_，创建后不可改',
    `word`        varchar(128) not null default '' comment '词条原文，可按拼写检索',
    `sort_no`     int          not null default 0 comment '在单词本中的顺序',
    `ext_info`    text                  default null comment '拓展信息(JSON)：phonetic、partOfSpeech、meaning、example、imageIds、audioIds 等',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    # 覆盖「按词本 + sort_no 分页」；uk 虽含 book_code，但无法避免 sort_no filesort
    key `idx_book_code_sort_no` (`book_code`, `sort_no`, `id`),
    unique key `uk_book_code_word_code` (`book_code`, `word_code`)
) engine = InnoDB
  default charset = utf8mb4 comment ='单词本词条';

# 已建库升级（按需执行一次）：
# alter table `admin_word_entry` drop index `idx_book_code`;
# alter table `admin_word_entry` add index `idx_book_code_sort_no` (`book_code`, `sort_no`, `id`);

insert into `admin_word_language` (`code`, `name`, `description`, `status`)
values ('EN', '英语', 'English，当前唯一启用语种', 1);

insert into `admin_word_category` (`language_code`, `code`, `name`, `description`, `sort_no`, `status`)
values ('EN', 'PRIMARY', '小学', '小学英语词汇', 10, 1),
       ('EN', 'JUNIOR', '初中', '初中英语词汇', 20, 1),
       ('EN', 'SENIOR', '高中', '高中英语词汇', 30, 1),
       ('EN', 'UNIVERSITY', '大学', '大学英语词汇（四六级、专四专八、商务英语等）', 40, 1),
       ('EN', 'KAOYAN', '考研', '考研英语词汇', 50, 1),
       ('EN', 'OVERSEAS', '出国', '出国/国际考试词汇（雅思、托福、GRE、SAT、GMAT等）', 60, 1);
