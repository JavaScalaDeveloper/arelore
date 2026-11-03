USE arelore_common;

# =============================================================================
# 通用意见反馈（客服工单，精简版）
# - 工单主表 + 消息表；附件二进制落 common_binary_file
# - 附件 file_id 列表、clientIp 等写入 message.ext_info（JSON）
# - 列表「最近互动」用 ticket.modify_time（发消息时需同步 bump 工单 modify_time）
# - product_code：word / prompt / site 等
# - 消息表无 create_time，时间以 modify_time / id 为准
# =============================================================================

# drop table if exists `common_feedback_message`;
# drop table if exists `common_feedback_ticket`;

# -----------------------------------------------------------------------------
# 工单主表：一单一行
# -----------------------------------------------------------------------------
CREATE TABLE `common_feedback_ticket`
(
    `id`            bigint auto_increment comment '主键ID',
    `create_time`   datetime              default current_timestamp not null comment '创建时间',
    `modify_time`   datetime              default current_timestamp not null on update current_timestamp comment '修改时间（有新消息时更新，列表排序用）',
    `user_id`       decimal(20, 0) not null comment '反馈人业务ID（与 user_registration_result.user_id 一致）',
    `product_code`  varchar(32)    not null default '' comment '产品编码：word/prompt/site 等',
    `title`         varchar(128)   not null default '' comment '标题/摘要（可由首条内容截断生成）',
    `status`        tinyint        not null default 1 comment '状态:1待处理(等客服),2处理中,3待用户回复,4已关闭',
    `admin_user_id` decimal(20, 0)          default null comment '当前处理客服业务ID，未接手为空',
    `ext_info`      text                    default null comment '拓展信息(JSON)：渠道/客户端版本/联系方式等',
    primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`),
    key `idx_user_id_product` (`user_id`, `product_code`),
    key `idx_product_status_modify` (`product_code`, `status`),
    key `idx_admin_user_id` (`admin_user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment ='通用意见反馈工单';

# -----------------------------------------------------------------------------
# 工单消息：用户与客服多轮交互
# ext_info 约定示例：
#   {"clientIp":"1.2.3.4","files":[{"fileId":123,"fileName":"a.png","contentType":"image/png"}]}
# -----------------------------------------------------------------------------
CREATE TABLE `common_feedback_message`
(
    `id`             bigint auto_increment comment '主键ID',
    `modify_time`    datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
    `ticket_id`      bigint         not null comment '工单ID（common_feedback_ticket.id）',
    `sender_type`    varchar(16)    not null comment '发送方: USER/ADMIN',
    `sender_user_id` decimal(20, 0) not null comment '发送人业务ID：C端 user_id 或管理端操作员ID',
    `content_text`   text           not null comment '文本内容；仅截图时可为空字符串',
    `ext_info`       text                    default null comment '拓展信息(JSON)：clientIp、files[{fileId,fileName,contentType}]',
    primary key (`id`),
    key `idx_modify_time` (`modify_time`),
    key `idx_ticket_id_id` (`ticket_id`, `id`)
) engine = InnoDB
  default charset = utf8mb4 comment ='意见反馈工单消息';
