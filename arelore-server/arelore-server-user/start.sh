#!/bin/bash

# ==============================================================================
# Arelore Server User Service 启动脚本
# ==============================================================================
# 功能说明：
#   本脚本用于在 CentOS 服务器上后台运行 Arelore 服务器用户服务
#   支持启动、停止、重启、查看状态和日志等功能
#   如果不传参数，默认执行启动命令
#   支持环境切换：dev（开发）、test（测试）、pre（预发）、prd（生产）
#
# 应用信息：
#   - 应用名称：arelore-server-user
#   - 默认端口：8081
#   - 日志文件：/var/log/arelore/arelore-server-user.log
#   - PID 文件：/var/log/arelore/arelore-server-user.pid
#
# 使用方法：
#   ./start.sh [环境]             # 默认启动应用，环境可选：dev/test/pre/prd
#   ./start.sh start [环境]       # 启动应用，环境可选：dev/test/pre/prd
#   ./start.sh stop              # 停止应用
#   ./start.sh restart [环境]     # 重启应用，环境可选：dev/test/pre/prd
#   ./start.sh status            # 查看应用状态
#   ./start.sh logs              # 查看应用日志
#   ./start.sh help              # 显示帮助信息
#
# 示例：
#   ./start.sh prd               # 生产环境启动
#   ./start.sh start dev         # 开发环境启动
#   ./start.sh restart pre       # 预发环境重启
# ==============================================================================

# 应用配置
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="arelore-server-user"
LOG_DIR="${APP_DIR}/logs"
LOG_FILE="${LOG_DIR}/${APP_NAME}.log"
PID_FILE="${LOG_DIR}/${APP_NAME}.pid"
PORT=8081

# 默认环境为 dev
ENVIRONMENT="dev"

# 环境配置文件
APPLICATION_DEV="application.yml"
APPLICATION_TEST="application-test.yml"
APPLICATION_PRE="application-pre.yml"
APPLICATION_PRD="application-prd.yml"

# 创建日志目录
mkdir -p $LOG_DIR

# 启动应用
start_app() {
    local env_param="$1"
    
    # 设置环境（如果提供了环境参数）
    if [ -n "$env_param" ]; then
        case "$env_param" in
            dev)
                ENVIRONMENT="dev"
                ;;
            test)
                ENVIRONMENT="test"
                ;;
            pre)
                ENVIRONMENT="pre"
                ;;
            prd)
                ENVIRONMENT="prd"
                ;;
            *)
                echo "警告: 未知环境 '$env_param'，使用默认环境: dev"
                ENVIRONMENT="dev"
                ;;
        esac
    fi
    
    echo "启动 ${APP_NAME} (环境: $ENVIRONMENT)..."
    
    # 进入应用目录
    cd "$APP_DIR" || { echo "无法切换到应用目录: $APP_DIR"; return 1; }
    
    # 先安装父模块和依赖（只执行一次）
    if [ ! -d "../target" ]; then
        echo "首次运行，安装父模块和依赖..."
        cd ..
        mvn clean install -DskipTests
        if [ $? -ne 0 ]; then
            echo "❌ 父模块安装失败，请检查网络或依赖配置"
            return 1
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
        return 1
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
            if ps -p $OLD_PID > /dev/null; then
                echo "强制停止进程..."
                kill -9 $OLD_PID
                sleep 1
            fi
        fi
        rm -f $PID_FILE
    fi
    
    # 查找 JAR 文件
    JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)
    
    if [ -z "$JAR_FILE" ]; then
        echo ""
        echo "❌ 未找到 JAR 文件"
        echo "请检查 Maven 是否正确打包"
        return 1
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
        return 1
    fi
    
    echo "✅ JAR 文件验证通过"
    
    # 根据环境选择配置文件
    case "$ENVIRONMENT" in
        dev)
            ACTIVE_PROFILE="dev"
            CONFIG_FILE="$APPLICATION_DEV"
            ;;
        test)
            ACTIVE_PROFILE="test"
            CONFIG_FILE="$APPLICATION_TEST"
            ;;
        pre)
            ACTIVE_PROFILE="pre"
            CONFIG_FILE="$APPLICATION_PRE"
            ;;
        prd)
            ACTIVE_PROFILE="prd"
            CONFIG_FILE="$APPLICATION_PRD"
            ;;
        *)
            ACTIVE_PROFILE="dev"
            CONFIG_FILE="$APPLICATION_DEV"
            ;;
    esac
    
    # 检查配置文件是否存在
    if [ ! -f "src/main/resources/$CONFIG_FILE" ]; then
        echo ""
        echo "⚠️  警告：配置文件不存在: src/main/resources/$CONFIG_FILE"
        echo "将使用默认配置"
    fi
    
    # 后台启动服务
    echo ""
    echo "=========================================="
    echo "启动服务..."
    echo "=========================================="
    echo "应用名称：$APP_NAME"
    echo "环境：$ENVIRONMENT"
    echo "激活配置：$ACTIVE_PROFILE"
    echo "日志文件：$LOG_FILE"
    echo "进程文件：$PID_FILE"
    echo "访问地址：http://localhost:$PORT"
    echo ""
    
    # 启动并实时输出日志
    nohup java -jar "$JAR_FILE" --spring.profiles.active="$ACTIVE_PROFILE" > "$LOG_FILE" 2>&1 &
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
        echo "   环境：$ENVIRONMENT"
        echo "   日志：$LOG_FILE"
        echo "   端口：$PORT"
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
        return 0
    else
        echo ""
        echo "=========================================="
        echo "❌ 服务启动失败"
        echo "=========================================="
        echo "请查看完整日志："
        echo "  cat $LOG_FILE"
        echo "或："
        echo "  tail -100 $LOG_FILE"
        rm -f $PID_FILE
        return 1
    fi
}

# 停止应用
stop_app() {
    echo "停止 ${APP_NAME}..."
    
    if [ ! -f "$PID_FILE" ]; then
        echo "${APP_NAME} 未运行"
        return 1
    fi
    
    PID=$(cat "$PID_FILE")
    
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "进程已不存在，清理PID文件..."
        rm -f "$PID_FILE"
        return 1
    fi
    
    # 尝试优雅停止
    kill "$PID"
    
    # 等待进程退出
    echo "等待进程退出..."
    for i in {1..10}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "${APP_NAME} 已停止"
            rm -f "$PID_FILE"
            return 0
        fi
        sleep 1
    done
    
    # 强制停止
    echo "强制停止进程..."
    kill -9 "$PID"
    rm -f "$PID_FILE"
    echo "${APP_NAME} 已强制停止"
    return 0
}

# 查看状态
status_app() {
    if [ ! -f "$PID_FILE" ]; then
        echo "${APP_NAME} 未运行"
        return 1
    fi
    
    PID=$(cat "$PID_FILE")
    
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "${APP_NAME} 正在运行"
        echo "PID: $PID"
        echo "日志文件: $LOG_FILE"
        return 0
    else
        echo "${APP_NAME} 已停止（残留PID文件）"
        rm -f "$PID_FILE"
        return 1
    fi
}

# 查看日志
logs_app() {
    if [ -f "$LOG_FILE" ]; then
        echo "显示 ${APP_NAME} 日志（最后50行）:"
        tail -n 50 "$LOG_FILE"
    else
        echo "日志文件不存在: $LOG_FILE"
        return 1
    fi
}

# 重启应用
restart_app() {
    local env_param="$1"
    stop_app
    sleep 2
    start_app "$env_param"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start     启动应用"
    echo "  stop      停止应用"
    echo "  restart   重启应用"
    echo "  status    查看应用状态"
    echo "  logs      查看应用日志"
    echo "  help      显示帮助信息"
    echo ""
    echo "环境参数:"
    echo "  dev       开发环境"
    echo "  test      测试环境"
    echo "  pre       预发环境"
    echo "  prd       生产环境"
    echo ""
    echo "示例:"
    echo "  $0 start    # 启动应用（默认dev环境）"
    echo "  $0 start prd    # 生产环境启动"
    echo "  $0 stop     # 停止应用"
    echo "  $0 restart test # 测试环境重启"
    echo "  $0 logs     # 查看日志"
}

# 主函数
main() {
    # 如果没有传参数，默认执行 start 命令
    if [ -z "$1" ]; then
        start_app
        return
    fi
    
    # 检查第一个参数是否为环境参数（dev/test/pre/prd）
    case "$1" in
        dev|test|pre|prd)
            start_app "$1"
            return
            ;;
    esac
    
    # 处理命令参数
    case "$1" in
        start)
            start_app "$2"
            ;;
        stop)
            stop_app
            ;;
        restart)
            restart_app "$2"
            ;;
        status)
            status_app
            ;;
        logs)
            logs_app
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
