#!/bin/bash

# ==============================================================================
# Arelore Server Admin 启动脚本
# ==============================================================================
# 同机多环境端口约定（无网络隔离时靠端口隔离）：
#
#   环境   admin-api  admin-web  user-api  user-web
#   prd    8082       3001       8081      3000
#   pre    8182       3101       8181      3100
#   test   8282       3201       8281      3200
#   dev    8382       3301       8381      3300
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
APP_NAME="arelore-server-admin"
LOG_DIR="${APP_DIR}/logs"
ENVIRONMENT="test"

APPLICATION_DEV="application.yml"
APPLICATION_TEST="application-test.yml"
APPLICATION_PRE="application-pre.yml"
APPLICATION_PRD="application-prd.yml"

mkdir -p "$LOG_DIR"

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

apply_env_ports() {
    case "$ENVIRONMENT" in
        prd) PORT=8082 ;;
        pre) PORT=8182 ;;
        test) PORT=8282 ;;
        dev) PORT=8382 ;;
        *) PORT=8282 ;;
    esac
    LOG_FILE="${LOG_DIR}/${APP_NAME}-${ENVIRONMENT}.log"
    PID_FILE="${LOG_DIR}/${APP_NAME}-${ENVIRONMENT}.pid"
}

start_app() {
    resolve_env "$1"
    apply_env_ports

    echo "启动 ${APP_NAME} (环境: $ENVIRONMENT)..."
    echo "端口: $PORT"

    cd "$APP_DIR" || { echo "无法进入目录: $APP_DIR"; return 1; }

    if [ ! -d "../target" ]; then
        echo "首次运行，安装父模块和依赖..."
        cd ..
        mvn clean install -DskipTests || { echo "父模块安装失败"; return 1; }
        cd arelore-server-admin
    fi

    echo "Maven 构建中..."
    mvn clean package -DskipTests || { echo "Maven 构建失败"; return 1; }

    if [ -f "$PID_FILE" ]; then
        OLD_PID=$(cat "$PID_FILE")
        if ps -p "$OLD_PID" > /dev/null 2>&1; then
            echo "停止旧进程 (PID: $OLD_PID)..."
            kill "$OLD_PID"
            sleep 2
            if ps -p "$OLD_PID" > /dev/null 2>&1; then
                kill -9 "$OLD_PID"
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

    JAR_FILE=$(find target -name "*.jar" -type f ! -name "*-sources.jar" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo "未找到 JAR 文件"
        return 1
    fi

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
            ACTIVE_PROFILE="test"
            CONFIG_FILE="$APPLICATION_TEST"
            ;;
    esac

    if [ ! -f "src/main/resources/$CONFIG_FILE" ]; then
        echo "警告: 配置文件不存在: src/main/resources/$CONFIG_FILE"
    fi

    echo "JAR: $JAR_FILE"
    echo "Profile: $ACTIVE_PROFILE"
    echo "日志: $LOG_FILE"

    nohup java -jar "$JAR_FILE" \
        --spring.profiles.active="$ACTIVE_PROFILE" \
        --server.port="$PORT" \
        > "$LOG_FILE" 2>&1 &
    NEW_PID=$!
    echo "$NEW_PID" > "$PID_FILE"

    sleep 3
    echo "最新日志 (末 50 行):"
    echo "----------------------------------------"
    tail -n 50 "$LOG_FILE"

    if ps -p "$NEW_PID" > /dev/null 2>&1; then
        echo "${APP_NAME}[$ENVIRONMENT] 已启动 PID=$NEW_PID 端口=$PORT"
        echo "访问: http://127.0.0.1:$PORT"
        if grep -q "Started.*Application" "$LOG_FILE"; then
            echo "Spring Boot 启动成功"
        else
            echo "Spring Boot 可能仍在启动，请稍后查看日志"
        fi
        return 0
    fi
    echo "启动失败，见: $LOG_FILE"
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
        if [ -f "${LOG_DIR}/${APP_NAME}-${env_name}.pid" ]; then
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
        local pf="${LOG_DIR}/${APP_NAME}-${env_name}.pid"
        if [ -f "$pf" ] && ps -p "$(cat "$pf")" > /dev/null 2>&1; then
            ENVIRONMENT="$env_name"
            apply_env_ports
            echo "${APP_NAME}[$env_name] 运行中 PID=$(cat "$pf") 端口=$PORT"
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

端口: prd=8082 pre=8182 test=8282 dev=8382

示例:
  $0 test
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
