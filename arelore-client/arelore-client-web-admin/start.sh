#!/bin/bash

# ==============================================================================
# Arelore 管理端 Web 启动脚本
# ==============================================================================
# 同机多环境部署时端口必须区分（无网络隔离时靠端口隔离）：
#
#   环境   admin-web  admin-api   user-web  user-api
#   prd    3001       8082        3000      8081
#   pre    3101       8182        3100      8181
#   test   3201       8282        3200      8281
#   dev    3301       8382        3300      8381
#
# 使用方法：
#   ./start.sh [环境]              # 启动，环境：dev/test/pre/prd（默认 test）
#   ./start.sh start [环境]
#   ./start.sh stop [环境]         # 不传环境则停止本应用全部实例
#   ./start.sh restart [环境]
#   ./start.sh status [环境]
#   ./start.sh logs [环境]
#   ./start.sh help
# ==============================================================================

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="arelore-client-web-admin"
ENVIRONMENT="test"

resolve_env() {
    local env_param="$1"
    if [ -z "$env_param" ]; then
        return 0
    fi
    case "$env_param" in
        dev|test|pre|prd)
            ENVIRONMENT="$env_param"
            ;;
        *)
            echo "警告: 未知环境 '$env_param'，使用: $ENVIRONMENT"
            ;;
    esac
}

# 按环境设置前端端口与后端 API
apply_env_ports() {
    case "$ENVIRONMENT" in
        prd)
            PORT=3001
            API_BASE_URL="http://127.0.0.1:8082/api"
            ;;
        pre)
            PORT=3101
            API_BASE_URL="http://127.0.0.1:8182/api"
            ;;
        test)
            PORT=3201
            API_BASE_URL="http://127.0.0.1:8282/api"
            ;;
        dev)
            PORT=3301
            API_BASE_URL="http://127.0.0.1:8382/api"
            ;;
        *)
            PORT=3201
            API_BASE_URL="http://127.0.0.1:8282/api"
            ;;
    esac
    LOG_FILE="$APP_DIR/app-${ENVIRONMENT}.log"
    PID_FILE="$APP_DIR/app-${ENVIRONMENT}.pid"
}

start_app() {
    resolve_env "$1"
    apply_env_ports

    echo "启动 ${APP_NAME} (环境: $ENVIRONMENT)..."
    echo "端口: $PORT"
    echo "API:  $API_BASE_URL"

    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "${APP_NAME}[$ENVIRONMENT] 已在运行 (PID: $PID)，先停止..."
            kill "$PID" 2>/dev/null
            sleep 2
            if ps -p "$PID" > /dev/null 2>&1; then
                kill -9 "$PID" 2>/dev/null
                sleep 1
            fi
        fi
        rm -f "$PID_FILE"
    fi

    PORT_PIDS=$(lsof -t -i:"$PORT" 2>/dev/null)
    if [ -n "$PORT_PIDS" ]; then
        echo "端口 $PORT 被占用，正在释放..."
        for P in $PORT_PIDS; do
            kill "$P" 2>/dev/null
            sleep 0.5
            if ps -p "$P" > /dev/null 2>&1; then
                kill -9 "$P" 2>/dev/null
            fi
        done
    fi

    cd "$APP_DIR" || { echo "无法进入目录: $APP_DIR"; return 1; }

    if ! command -v npm &> /dev/null; then
        echo "错误: npm 未安装"
        return 1
    fi

    if [ ! -d "node_modules" ]; then
        echo "正在安装依赖..."
        npm install || { echo "依赖安装失败"; return 1; }
    fi

    echo "正在启动应用..."
    export PORT
    export REACT_APP_API_BASE_URL="$API_BASE_URL"
    export BROWSER=none
    nohup npm start > "$LOG_FILE" 2>&1 &
    PID=$!
    echo "$PID" > "$PID_FILE"

    sleep 5
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "${APP_NAME}[$ENVIRONMENT] 启动成功"
        echo "PID: $PID"
        echo "访问: http://127.0.0.1:$PORT"
        echo "日志: $LOG_FILE"
        return 0
    fi
    echo "${APP_NAME}[$ENVIRONMENT] 启动失败，见日志: $LOG_FILE"
    rm -f "$PID_FILE"
    return 1
}

stop_one() {
    local env_name="$1"
    ENVIRONMENT="$env_name"
    apply_env_ports
    if [ ! -f "$PID_FILE" ]; then
        echo "${APP_NAME}[$env_name] 未运行"
        return 1
    fi
    PID=$(cat "$PID_FILE")
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "${APP_NAME}[$env_name] 进程不存在，清理 PID"
        rm -f "$PID_FILE"
        return 1
    fi
    echo "停止 ${APP_NAME}[$env_name] (PID: $PID)..."
    kill "$PID" 2>/dev/null
    for _ in {1..10}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            rm -f "$PID_FILE"
            echo "${APP_NAME}[$env_name] 已停止"
            return 0
        fi
        sleep 1
    done
    kill -9 "$PID" 2>/dev/null
    rm -f "$PID_FILE"
    echo "${APP_NAME}[$env_name] 已强制停止"
    return 0
}

stop_app() {
    if [ -n "$1" ]; then
        resolve_env "$1"
        stop_one "$ENVIRONMENT"
        return $?
    fi
    local any=0
    for env_name in prd pre test dev; do
        if [ -f "$APP_DIR/app-${env_name}.pid" ]; then
            stop_one "$env_name"
            any=1
        fi
    done
    if [ "$any" -eq 0 ]; then
        echo "${APP_NAME} 无运行中的实例"
        return 1
    fi
    return 0
}

status_app() {
    if [ -n "$1" ]; then
        resolve_env "$1"
        apply_env_ports
        if [ -f "$PID_FILE" ] && ps -p "$(cat "$PID_FILE")" > /dev/null 2>&1; then
            echo "${APP_NAME}[$ENVIRONMENT] 运行中 PID=$(cat "$PID_FILE") 端口=$PORT"
            return 0
        fi
        echo "${APP_NAME}[$ENVIRONMENT] 未运行"
        return 1
    fi
    local found=0
    for env_name in prd pre test dev; do
        local pf="$APP_DIR/app-${env_name}.pid"
        if [ -f "$pf" ] && ps -p "$(cat "$pf")" > /dev/null 2>&1; then
            ENVIRONMENT="$env_name"
            apply_env_ports
            echo "${APP_NAME}[$env_name] 运行中 PID=$(cat "$pf") 端口=$PORT API=$API_BASE_URL"
            found=1
        fi
    done
    if [ "$found" -eq 0 ]; then
        echo "${APP_NAME} 无运行中的实例"
        return 1
    fi
    return 0
}

logs_app() {
    resolve_env "${1:-$ENVIRONMENT}"
    apply_env_ports
    if [ -f "$LOG_FILE" ]; then
        echo "=== ${APP_NAME}[$ENVIRONMENT] 日志（末 50 行）==="
        tail -n 50 "$LOG_FILE"
    else
        echo "日志不存在: $LOG_FILE"
        return 1
    fi
}

restart_app() {
    resolve_env "${1:-$ENVIRONMENT}"
    stop_one "$ENVIRONMENT"
    sleep 2
    start_app "$ENVIRONMENT"
}

show_help() {
    cat <<EOF
用法: $0 [命令] [环境]

命令: start | stop | restart | status | logs | help
环境: prd | pre | test | dev（默认 test）

端口约定（同机多环境）:
  prd  admin-web=3001  admin-api=8082
  pre  admin-web=3101  admin-api=8182
  test admin-web=3201  admin-api=8282
  dev  admin-web=3301  admin-api=8382

示例:
  $0 test              # 启动 test
  $0 start prd
  $0 stop test
  $0 status
  $0 logs test
EOF
}

main() {
    if [ -z "$1" ]; then
        start_app "$ENVIRONMENT"
        return
    fi
    case "$1" in
        dev|test|pre|prd)
            start_app "$1"
            ;;
        start)
            start_app "${2:-$ENVIRONMENT}"
            ;;
        stop)
            stop_app "$2"
            ;;
        restart)
            restart_app "${2:-$ENVIRONMENT}"
            ;;
        status)
            status_app "$2"
            ;;
        logs)
            logs_app "$2"
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

main "$@"
