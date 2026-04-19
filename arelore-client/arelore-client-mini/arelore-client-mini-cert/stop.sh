#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ ! -f ".taro-dev.pid" ]]; then
  echo "[mini-cert] no pid file: .taro-dev.pid"
  exit 0
fi

PID="$(cat .taro-dev.pid || true)"
if [[ -z "${PID:-}" ]]; then
  echo "[mini-cert] empty pid"
  rm -f .taro-dev.pid
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

rm -f .taro-dev.pid
echo "[mini-cert] stopped"

