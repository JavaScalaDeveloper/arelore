#!/usr/bin/env bash
# 按环境写入 utils/env.local.js，小程序重新编译后即生效。
# 用法（Git Bash / macOS / Linux）：
#   bash start.sh dev
#   bash start.sh test
# Windows PowerShell（勿用系统 sh/bash，请指定 Git 自带的 bash）：
#   & "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
#   & "C:\Program Files\Git\bin\bash.exe" start.sh dev
set -euo pipefail

ENV_NAME="${1:-dev}"
APP_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$APP_DIR"

API_BASE_URL=""
case "$ENV_NAME" in
  dev)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  test)
    API_BASE_URL="http://www.arelore.com:8281/api"
    ;;
  pre)
    API_BASE_URL="http://localhost:8081/api"
    ;;
  prd)
    API_BASE_URL="http://www.arelore.com:8081/api"
    ;;
  *)
    API_BASE_URL="$ENV_NAME"
    ENV_NAME="custom"
    ;;
esac

if [ -n "${MINI_WORD_API_BASE_URL:-}" ]; then
  API_BASE_URL="$MINI_WORD_API_BASE_URL"
fi

ENV_FILE="${APP_DIR}/utils/env.local.js"
cat > "$ENV_FILE" <<EOF
/** 由 start.sh 自动生成，请勿手改后提交（已 gitignore） */
module.exports = {
  env: '${ENV_NAME}',
  apiBaseUrl: '${API_BASE_URL}'
};
EOF

echo "[mini-word] 已写入 utils/env.local.js"
echo "[mini-word] env=${ENV_NAME}"
echo "[mini-word] apiBaseUrl=${API_BASE_URL}"
echo ""
echo "下一步（必须）："
echo "  1. 打开微信开发者工具，导入本目录"
echo "  2. 若 Storage 里有 miniApiBaseUrl / miniApiEnv，先删掉（否则会覆盖本文件）"
echo "  3. 点击「编译」重新加载小程序"
echo ""
echo "说明："
echo "  - 本脚本不会启动后端；请先确保 user 服务在对应端口可访问"
echo "  - 模拟器访问本机请用 localhost/127.0.0.1；真机调试本机需用电脑局域网 IP"
echo "  - 临时覆盖仍可用 Storage：miniApiEnv=dev 或 miniApiBaseUrl=完整地址"
