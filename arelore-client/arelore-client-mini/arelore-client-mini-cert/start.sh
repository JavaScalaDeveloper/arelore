#!/usr/bin/env bash
# 请用 bash 执行：./start.sh test 或 bash start.sh test
# （用 sh 调用时，部分系统上的 sh 不支持 set -o pipefail，可能直接报错退出）
set -euo pipefail

ENV_NAME="${1:-dev}"

cd "$(dirname "$0")"

API_BASE_URL=""
case "$ENV_NAME" in
  dev)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  test)
    # 分支默认值勿与「当前 shell 里已 export 的 TARO_APP_API_BASE_URL」混用，否则 test 也会误用 8081 等地址
    API_BASE_URL="http://www.arelore.com:8281/api"
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

nohup npm run dev:weapp >/var/logs/arelore-mini-cert-dev.log 2>&1 &
echo $! > .taro-dev.pid

echo "[mini-cert] started. pid=$(cat .taro-dev.pid)"
echo "[mini-cert] dev log: /var/logs/arelore-mini-cert-dev.log"
echo "[mini-cert] 微信开发者工具请导入本目录（project.config.json 已指向 dist/），勿直接打开 src/"

