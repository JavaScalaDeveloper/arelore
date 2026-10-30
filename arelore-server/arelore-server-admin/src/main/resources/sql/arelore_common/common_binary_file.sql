
USE arelore_common;

# hash_value 存 SHA-256 原始 32 字节（BINARY），业务侧/ext_json 里常见为 64 位 hex。
# 按内容 hash 查询须 UNHEX，例如（meaningAudio.hashValue / fileHash）：
#   SELECT id, create_time, modify_time, HEX(hash_value) AS hash_hex, LENGTH(file_data) AS bytes, status
#   FROM arelore_common.common_binary_file
#   WHERE hash_value = UNHEX('8224e9f85462a82d55e76b31ddb10d3eaedba14241e8f6608ea43c23ff0fe467');
# 直接 WHERE hash_value = '8224e9f8…'（字符串）查不到。按主键更直接：WHERE id = <fileId>。

CREATE TABLE IF NOT EXISTS common_binary_file
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键ID'
    PRIMARY KEY,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    hash_value  BINARY(32) NOT NULL COMMENT '文件SHA-256原始32字节；对账用HEX(hash_value)；按hex查须 UNHEX(hex)',
    file_data   LONGBLOB   NOT NULL COMMENT '文件二进制内容',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1有效,0无效',
    ext_json    TEXT NULL COMMENT '扩展信息(JSON格式)',
    CONSTRAINT uk_hash_status UNIQUE (hash_value)
    ) ENGINE=InnoDB
    ROW_FORMAT=DYNAMIC
    COMMENT='通用二进制文件存储表';
CREATE INDEX idx_create_time
    ON common_binary_file (create_time);
