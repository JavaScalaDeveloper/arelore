#!/bin/bash

# ==============================================================================
# Arelore 客户端应用启动脚本
# ==============================================================================
# 功能说明：
#   本脚本用于在 CentOS 服务器上后台运行 Arelore 客户端 Web 应用
#   支持启动、停止、重启、查看状态和日志等功能
#   如果不传参数，默认执行启动命令
#   支持环境切换：dev（开发）、test（测试）、pre（预发）、prd（生产）
#
# 应用信息：
#   - 应用名称：arelore-client-web
#   - 默认端口：3000
#   - 日志文件：app.log
#   - PID 文件：app.pid
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
APP_NAME="arelore-client-web"
LOG_FILE="$APP_DIR/app.log"
PID_FILE="$APP_DIR/app.pid"
PORT=3000

# 默认环境为 dev
ENVIRONMENT="dev"

# API 基础 URL 配置
API_URL_DEV="http://localhost:8081/api"
API_URL_TEST="http://test.arelore.com/api"
API_URL_PRE="http://pre.arelore.com/api"
API_URL_PRD="http://www.arelore.com/api"

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
    
    # 根据环境设置 API 基础 URL
    case "$ENVIRONMENT" in
        dev)
            API_BASE_URL="$API_URL_DEV"
            ;;
        test)
            API_BASE_URL="$API_URL_TEST"
            ;;
        pre)
            API_BASE_URL="$API_URL_PRE"
            ;;
        prd)
            API_BASE_URL="$API_URL_PRD"
            ;;
        *)
            API_BASE_URL="$API_URL_DEV"
            ;;
    esac
    
    echo "启动 ${APP_NAME} (环境: $ENVIRONMENT)..."
    echo "API 基础 URL: $API_BASE_URL"
    
    # 检查是否已运行
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "${APP_NAME} 已在运行 (PID: $PID)"
            return 1
        else
            echo "发现残留的PID文件，正在清理..."
            rm -f "$PID_FILE"
        fi
    fi
    
    # 切换到应用目录
    cd "$APP_DIR" || { echo "无法切换到应用目录: $APP_DIR"; return 1; }
    
    # 检查npm是否安装
    if ! command -v npm &> /dev/null; then
        echo "错误: npm 未安装"
        return 1
    fi
    
    # 检查依赖是否已安装
    if [ ! -d "node_modules" ]; then
        echo "正在安装依赖..."
        npm install
        if [ $? -ne 0 ]; then
            echo "依赖安装失败"
            return 1
        fi
    fi
    
    # 启动应用（后台运行），设置环境变量
    echo "正在启动应用..."
    export REACT_APP_API_BASE_URL="$API_BASE_URL"
    nohup npm start > "$LOG_FILE" 2>&1 &
    PID=$!
    
    # 保存PID
    echo "$PID" > "$PID_FILE"
    
    # 等待应用启动
    echo "等待应用启动..."
    sleep 5
    
    # 检查应用是否成功启动
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "${APP_NAME} 启动成功！"
        echo "PID: $PID"
        echo "环境: $ENVIRONMENT"
        echo "API 基础 URL: $API_BASE_URL"
        echo "日志文件: $LOG_FILE"
        echo "请访问: http://localhost:$PORT"
        return 0
    else
        echo "${APP_NAME} 启动失败！"
        echo "请查看日志: $LOG_FILE"
        rm -f "$PID_FILE"
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
    echo "示例:"
    echo "  $0 start    # 启动应用"
    echo "  $0 stop     # 停止应用"
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