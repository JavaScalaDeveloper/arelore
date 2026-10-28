#!/usr/bin/env bash
# 请用 bash 执行：./start.sh test 或 bash start.sh test
# （用 sh 调用时，部分系统上的 sh 不支持 set -o pipefail，可能直接报错退出）
# Windows PowerShell 请用 Git 自带的 bash（勿用系统 sh/bash，常指向 WSL 空壳）：
#   & "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
#   & "C:\Program Files\Git\bin\bash.exe" start.sh test
set -euo pipefail

ENV_NAME="${1:-dev}"

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
CLIENT_ROOT="$(cd "${APP_DIR}/../.." && pwd)"
LOG_DIR="${CLIENT_ROOT}/logs"
APP_NAME="arelore-client-mini-cert"
mkdir -p "$LOG_DIR"

LOG_FILE="${LOG_DIR}/${APP_NAME}-${ENV_NAME}.log"
PID_FILE="${LOG_DIR}/${APP_NAME}-${ENV_NAME}.pid"

cd "$APP_DIR"

API_BASE_URL=""
case "$ENV_NAME" in
  dev)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  test)
    # 分支默认值勿与「当前 shell 里已 export 的 TARO_APP_API_BASE_URL」混用，否则 test 也会误用 8081 等地址
    API_BASE_URL="http://test.arelore.com:8281/api"
    ;;
  pre)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  prd)
    API_BASE_URL="http://www.arelore.com:8081/api"
    ;;
  *)
    # 允许直接传入自定义 baseUrl，如：./start.sh "http://1.2.3.4:8081/api"
    API_BASE_URL="$ENV_NAME"
    ENV_NAME="custom"
    LOG_FILE="${LOG_DIR}/${APP_NAME}-${ENV_NAME}.log"
    PID_FILE="${LOG_DIR}/${APP_NAME}-${ENV_NAME}.pid"
    ;;
esac

# 任意环境：需要临时改域名/端口时优先用此变量（高于上面分支默认值）
if [ -n "${MINI_CERT_API_BASE_URL:-}" ]; then
  API_BASE_URL="$MINI_CERT_API_BASE_URL"
fi

echo "[mini-cert] env=$ENV_NAME apiBaseUrl=${API_BASE_URL:-<empty>}"
echo "[mini-cert] 若小程序仍请求旧地址：开发者工具里删掉 Storage 中的 miniApiBaseUrl（会覆盖构建注入的地址）"

export TARO_APP_API_BASE_URL="${API_BASE_URL:-}"

echo "[mini-cert] npm install"
npm install

echo "[mini-cert] rebuild & start watcher"
npm run build:weapp

if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "${OLD_PID:-}" ] && kill -0 "$OLD_PID" >/dev/null 2>&1; then
    echo "[mini-cert] stopping previous pid=$OLD_PID"
    kill "$OLD_PID" || true
    sleep 1
  fi
  rm -f "$PID_FILE"
fi

nohup npm run dev:weapp >"$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "[mini-cert] started. pid=$(cat "$PID_FILE")"
echo "[mini-cert] dev log: $LOG_FILE"
echo "[mini-cert] 微信开发者工具请导入本目录（project.config.json 已指向 dist/），勿直接打开 src/"
