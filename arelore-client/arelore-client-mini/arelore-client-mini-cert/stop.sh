#!/usr/bin/env bash
set -euo pipefail

ENV_NAME="${1:-dev}"
APP_DIR="$(cd "$(dirname "$0")" && pwd)"
CLIENT_ROOT="$(cd "${APP_DIR}/../.." && pwd)"
LOG_DIR="${CLIENT_ROOT}/logs"
APP_NAME="arelore-client-mini-cert"
PID_FILE="${LOG_DIR}/${APP_NAME}-${ENV_NAME}.pid"

# 兼容旧路径
LEGACY_PID_FILE="${APP_DIR}/.taro-dev.pid"

if [[ ! -f "$PID_FILE" && -f "$LEGACY_PID_FILE" ]]; then
  PID_FILE="$LEGACY_PID_FILE"
fi

if [[ ! -f "$PID_FILE" ]]; then
  echo "[mini-cert] no pid file: ${LOG_DIR}/${APP_NAME}-${ENV_NAME}.pid"
  exit 0
fi

PID="$(cat "$PID_FILE" || true)"
if [[ -z "${PID:-}" ]]; then
  echo "[mini-cert] empty pid"
  rm -f "$PID_FILE"
  exit 0
fi

if kill -0 "$PID" >/dev/null 2>&1; then
  echo "[mini-cert] stopping pid=$PID"
  kill "$PID" || true
  sleep 1
  if kill -0 "$PID" >/dev/null 2>&1; then
    echo "[mini-cert] force killing pid=$PID"
    kill -9 "$PID" || true
  fi
else
  echo "[mini-cert] pid not running: $PID"
fi

rm -f "$PID_FILE" "$LEGACY_PID_FILE"
echo "[mini-cert] stopped"
