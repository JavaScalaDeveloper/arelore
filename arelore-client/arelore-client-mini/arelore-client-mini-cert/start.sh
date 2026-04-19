#!/usr/bin/env bash
set -euo pipefail

ENV_NAME="${1:-dev}"

cd "$(dirname "$0")"

API_BASE_URL=""
case "$ENV_NAME" in
  dev)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  prd|prod)
    API_BASE_URL="${TARO_APP_API_BASE_URL:-}"
    ;;
  *)
    # 允许直接传入自定义 baseUrl，如：./start.sh "http://1.2.3.4:8081/api"
    API_BASE_URL="$ENV_NAME"
    ENV_NAME="custom"
    ;;
esac

echo "[mini-cert] env=$ENV_NAME apiBaseUrl=${API_BASE_URL:-<empty>}"

export TARO_APP_API_BASE_URL="${API_BASE_URL:-}"

echo "[mini-cert] npm install"
npm install

echo "[mini-cert] rebuild & start watcher"
npm run build:weapp

nohup npm run dev:weapp >/tmp/arelore-mini-cert-dev.log 2>&1 &
echo $! > .taro-dev.pid

echo "[mini-cert] started. pid=$(cat .taro-dev.pid)"
echo "[mini-cert] dev log: /tmp/arelore-mini-cert-dev.log"

