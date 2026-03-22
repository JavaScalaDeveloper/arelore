#!/bin/bash

# Arelore 日志自动清理脚本
# 功能：自动删除 30 天前的日志文件
# 配置到 crontab 中实现定时清理

LOG_DIR="/var/log/arelore"
DAYS_TO_KEEP=30

# 检查日志目录是否存在
if [ ! -d "$LOG_DIR" ]; then
    echo "日志目录不存在：$LOG_DIR"
    exit 0
fi

echo "开始清理 $DAYS_TO_keep 天前的日志..."

# 查找并删除旧日志
find $LOG_DIR -name "*.log.*" -type f -mtime +$DAYS_TO_KEEP -delete

echo "日志清理完成"
