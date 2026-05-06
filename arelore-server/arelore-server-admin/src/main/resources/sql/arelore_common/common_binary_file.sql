
USE arelore_common;
CREATE TABLE IF NOT EXISTS common_binary_file
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键ID'
    PRIMARY KEY,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    hash_value  BINARY(32) NOT NULL COMMENT '文件Hash原始值(默认SHA-256=32字节)',
    file_data   LONGBLOB   NOT NULL COMMENT '文件二进制内容',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1有效,0无效',
    ext_json    TEXT NULL COMMENT '扩展信息(JSON格式)',
    CONSTRAINT uk_hash_status UNIQUE (hash_value)
    ) ENGINE=InnoDB
    ROW_FORMAT=DYNAMIC
    COMMENT='通用二进制文件存储表';
CREATE INDEX idx_create_time
    ON common_binary_file (create_time);
