#!/bin/bash

# Arelore Server User Service 启动脚本
# 端口：8081

APP_NAME="arelore-server-user"
LOG_DIR="/var/log/arelore"
LOG_FILE="${LOG_DIR}/${APP_NAME}.log"
PID_FILE="${LOG_DIR}/${APP_NAME}.pid"

# 创建日志目录
mkdir -p $LOG_DIR

echo "正在构建和启动 $APP_NAME..."

# 进入脚本所在目录
cd "$(dirname "$0")"

# 先安装父模块和依赖（只执行一次）
if [ ! -d "../target" ]; then
    echo "首次运行，安装父模块和依赖..."
    cd ..
    mvn clean install -DskipTests -q
    if [ $? -ne 0 ]; then
        echo "❌ 父模块安装失败，请检查网络或依赖配置"
        exit 1
    fi
    echo "✅ 父模块和依赖安装完成"
    cd arelore-server-user
fi

# Maven 构建
echo "Maven 构建中..."
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Maven 构建失败，请检查网络或依赖配置"
    exit 1
fi

echo "✅ Maven 构建成功"

# 停止旧进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat $PID_FILE)
    if ps -p $OLD_PID > /dev/null; then
        echo "停止旧进程 (PID: $OLD_PID)..."
        kill $OLD_PID
        sleep 2
    fi
    rm -f $PID_FILE
fi

# 查找 JAR 文件
JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ 未找到 JAR 文件"
    exit 1
fi

# 后台启动服务
echo "启动服务..."
nohup java -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
NEW_PID=$!

echo $NEW_PID > $PID_FILE

echo "✅ $APP_NAME 已启动"
echo "   PID: $NEW_PID"
echo "   日志：$LOG_FILE"
echo "   端口：8081"
echo ""
echo "查看日志：tail -f $LOG_FILE"
echo "停止服务：kill $NEW_PID"
