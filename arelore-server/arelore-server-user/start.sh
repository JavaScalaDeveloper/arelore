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
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ 父模块安装失败，请检查网络或依赖配置"
        exit 1
    fi
    echo "✅ 父模块和依赖安装完成"
    cd arelore-server-user
fi

# Maven 构建（显示详细日志）
echo ""
echo "=========================================="
echo "Maven 构建中..."
echo "=========================================="
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Maven 构建失败，请检查网络和依赖配置"
    echo "查看上面的错误日志进行排查"
    exit 1
fi

echo ""
echo "✅ Maven 构建成功"

# 停止旧进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat $PID_FILE)
    if ps -p $OLD_PID > /dev/null; then
        echo ""
        echo "停止旧进程 (PID: $OLD_PID)..."
        kill $OLD_PID
        sleep 2
    fi
    rm -f $PID_FILE
fi

# 查找 JAR 文件
JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo ""
    echo "❌ 未找到 JAR 文件"
    echo "请检查 Maven 是否正确打包"
    exit 1
fi

echo ""
echo "JAR 文件：$JAR_FILE"

# 验证 JAR 文件
echo ""
echo "验证 JAR 文件..."
unzip -l "$JAR_FILE" | grep -i "manifest"
if [ $? -ne 0 ]; then
    echo ""
    echo "⚠️  警告：JAR 文件中没有找到 MANIFEST.MF"
    echo "这可能导致 'no main manifest attribute' 错误"
    echo ""
    echo "解决方案："
    echo "1. 确认 pom.xml 中包含 spring-boot-maven-plugin"
    echo "2. 重新执行：mvn clean package"
    exit 1
fi

echo "✅ JAR 文件验证通过"

# 后台启动服务
echo ""
echo "=========================================="
echo "启动服务..."
echo "=========================================="
echo "应用名称：$APP_NAME"
echo "日志文件：$LOG_FILE"
echo "进程文件：$PID_FILE"
echo "访问地址：http://localhost:8081"
echo ""
echo "启动日志:"
echo "----------------------------------------"

# 启动并实时输出日志
nohup java -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
NEW_PID=$!

echo $NEW_PID > $PID_FILE

# 等待 3 秒，让服务初始化
sleep 3

# 显示启动日志的最后部分
echo ""
echo "最新日志 (最后 50 行):"
echo "----------------------------------------"
tail -50 $LOG_FILE

# 检查服务是否启动成功
if ps -p $NEW_PID > /dev/null; then
    echo ""
    echo "=========================================="
    echo "✅ $APP_NAME 已启动"
    echo "=========================================="
    echo "   PID: $NEW_PID"
    echo "   日志：$LOG_FILE"
    echo "   端口：8081"
    echo ""
    echo "查看实时日志：tail -f $LOG_FILE"
    echo "停止服务：kill $NEW_PID"
    echo ""
    
    # 检查 Spring Boot 是否启动成功
    if grep -q "Started.*Application" $LOG_FILE; then
        echo "✅ Spring Boot 启动成功!"
    else
        echo "⚠️  Spring Boot 可能还在启动中，请稍后查看日志"
    fi
else
    echo ""
    echo "=========================================="
    echo "❌ 服务启动失败"
    echo "=========================================="
    echo "请查看完整日志："
    echo "  cat $LOG_FILE"
    echo "或："
    echo "  tail -100 $LOG_FILE"
    exit 1
fi
