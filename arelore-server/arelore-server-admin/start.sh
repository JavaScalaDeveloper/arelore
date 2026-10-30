#!/bin/bash

# ==============================================================================
# Arelore Server Admin 启动脚本
# ==============================================================================
# 同机多环境端口约定（无网络隔离时靠端口隔离）：
#
#   环境   admin-api  admin-web  user-api  user-web
#   prd    8082       3101       8081      3000
#   pre    8182       3111       8181      3100
#   test   8282       3201       8281      3001
#   dev    8082       3301       8081      3300   （配置同 test，端口同 prd 便于本地联调）
#
# 使用方法：
#   ./start.sh [环境]              # 启动已有 JAR（默认不 Maven，避免打满轻量机磁盘 IO）
#   ./start.sh start [环境] [--rebuild|--clean-rebuild]
#   ./start.sh rebuild [环境]      # 仅构建（nice+ionice 降 IO 优先级；默认不 clean）
#   ./start.sh stop [环境]         # 不传环境则停止本应用全部实例
#   ./start.sh restart [环境] [--rebuild|--clean-rebuild]
#   ./start.sh status [环境]
#   ./start.sh logs [环境]
#   ./start.sh help
#
# 说明：阿里云轻量机系统盘 IOPS 很低；每次 mvn clean package 会大量读 .m2 / 写 target，
# 易把磁盘读 IO 打满导致 SSH 卡死。日常启动请用已有 JAR；改代码后再 rebuild。
# ==============================================================================

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${APP_DIR}/../.." && pwd)"
APP_NAME="arelore-server-admin"
LOG_DIR="${REPO_ROOT}/logs"
LOG_RETENTION_DAYS="${LOG_RETENTION_DAYS:-30}"
ENVIRONMENT="test"

APPLICATION_DEV="application-dev.yml"
APPLICATION_TEST="application-test.yml"
APPLICATION_PRE="application-pre.yml"
APPLICATION_PRD="application-prd.yml"

mkdir -p "$LOG_DIR"
# 清理超过保留天数的日志文件（保留 .pid）；logback 本身也有 maxHistory=30
find "$LOG_DIR" -type f \( -name '*.log' -o -name '*.log-*' \) -mtime +"${LOG_RETENTION_DAYS}" -delete 2>/dev/null || true

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
        dev) PORT=8082 ;;
        *) PORT=8282 ;;
    esac
    LOG_FILE="${LOG_DIR}/${APP_NAME}-${ENVIRONMENT}.log"
    PID_FILE="${LOG_DIR}/${APP_NAME}-${ENVIRONMENT}.pid"
}

# 降低构建时磁盘/CPU 抢占，减轻对 SSH 的影响（best-effort）
run_mvn() {
    if command -v ionice >/dev/null 2>&1; then
        nice -n 19 ionice -c2 -n7 mvn "$@"
    else
        nice -n 19 mvn "$@"
    fi
}

find_jar() {
    find target -name "${APP_NAME}-*.jar" -type f ! -name "*-sources.jar" 2>/dev/null | head -n 1
}

# do_clean=1 时 mvn clean；否则增量 package，少打磁盘
build_app() {
    local do_clean="${1:-0}"
    cd "$APP_DIR" || { echo "无法进入目录: $APP_DIR"; return 1; }

    if [ ! -d "../arelore-server-core/target" ] && [ ! -d "../target" ]; then
        echo "首次运行，安装父模块和依赖（耗磁盘 IO，请耐心等待）..."
        cd ..
        run_mvn install -DskipTests || { echo "父模块安装失败"; return 1; }
        cd arelore-server-admin
    fi

    if [ "$do_clean" = "1" ]; then
        echo "Maven clean package（全量，IO 很重）..."
        run_mvn clean package -DskipTests || { echo "Maven 构建失败"; return 1; }
    else
        echo "Maven package（增量，无 clean）..."
        run_mvn package -DskipTests || { echo "Maven 构建失败"; return 1; }
    fi

    JAR_FILE=$(find_jar)
    if [ -z "$JAR_FILE" ]; then
        echo "构建完成但未找到 JAR"
        return 1
    fi
    echo "构建完成: $JAR_FILE"
    return 0
}

# args: env [--rebuild|--clean-rebuild]
start_app() {
    local rebuild=0
    local clean_rebuild=0
    local env_arg=""
    for arg in "$@"; do
        case "$arg" in
            --rebuild|-r) rebuild=1 ;;
            --clean-rebuild) clean_rebuild=1; rebuild=1 ;;
            "") ;;
            *)
                if [ -z "$env_arg" ]; then
                    env_arg="$arg"
                fi
                ;;
        esac
    done
    resolve_env "$env_arg"
    apply_env_ports

    echo "启动 ${APP_NAME} (环境: $ENVIRONMENT)..."
    echo "端口: $PORT"

    cd "$APP_DIR" || { echo "无法进入目录: $APP_DIR"; return 1; }

    JAR_FILE=$(find_jar)
    if [ "$rebuild" = "1" ] || [ -z "$JAR_FILE" ]; then
        if [ -z "$JAR_FILE" ] && [ "$rebuild" != "1" ]; then
            echo "未找到 JAR，自动构建一次..."
        fi
        build_app "$clean_rebuild" || return 1
        JAR_FILE=$(find_jar)
    else
        echo "复用已有 JAR（跳过 Maven，避免磁盘 IO 打满）: $JAR_FILE"
        echo "若代码有变更请: $0 rebuild  或  $0 start $ENVIRONMENT --rebuild"
    fi

    if [ -z "$JAR_FILE" ]; then
        echo "未找到 JAR 文件"
        return 1
    fi

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

    # 导出 LOG_DIR 供 logback-spring.xml 写入仓库根 logs/
    export LOG_DIR
    nohup java -DLOG_DIR="$LOG_DIR" -jar "$JAR_FILE" \
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
    local env_arg=""
    local flags=()
    for arg in "$@"; do
        case "$arg" in
            --rebuild|-r|--clean-rebuild) flags+=("$arg") ;;
            *)
                if [ -z "$env_arg" ]; then
                    env_arg="$arg"
                fi
                ;;
        esac
    done
    resolve_env "${env_arg:-$ENVIRONMENT}"
    stop_one "$ENVIRONMENT"
    sleep 2
    start_app "$ENVIRONMENT" "${flags[@]}"
}

show_help() {
    cat <<EOF
用法: $0 [命令] [环境] [选项]

命令: start | stop | restart | rebuild | status | logs | help
环境: prd | pre | test | dev（默认 test）
选项: --rebuild / -r（增量 package）  --clean-rebuild（clean package，IO 最重）

端口: prd=8082 pre=8182 test=8282 dev=8082

注意: 默认启动不跑 Maven，直接用 target 下已有 JAR。
轻量云主机上反复 mvn clean package 易打满磁盘读 IO，导致 SSH 卡住。

示例:
  $0 test                         # 仅启动已有 JAR
  $0 start prd
  $0 start test --rebuild         # 改代码后增量构建再启动
  $0 rebuild                      # 只构建不启动
  $0 restart test
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
            env_name="$1"
            shift
            start_app "$env_name" "$@"
            ;;
        start)
            shift
            start_app "$@"
            ;;
        rebuild|build)
            shift
            env_arg=""
            do_clean=0
            for arg in "$@"; do
                case "$arg" in
                    --clean-rebuild|--clean) do_clean=1 ;;
                    --rebuild|-r) ;;
                    *)
                        if [ -z "$env_arg" ]; then
                            env_arg="$arg"
                        fi
                        ;;
                esac
            done
            resolve_env "$env_arg"
            build_app "$do_clean"
            ;;
        stop)
            stop_app "$2"
            ;;
        restart)
            shift
            restart_app "$@"
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
