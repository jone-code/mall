#!/usr/bin/env bash
# C 端 uni-app H5（默认 http://localhost:5174，避免与管理端 5173 冲突）
# 前提：mall-bff 已在 8001 运行
set -euo pipefail

cd "$(dirname "$0")/../frontend/app"

PORT="${APP_H5_PORT:-5174}"

if [ ! -d node_modules ]; then
  echo "首次运行，安装依赖..."
  pnpm install
fi

echo "启动 C 端 H5 -> http://localhost:$PORT"
echo "登录入口：底部「我的」或首页「去登录」"
echo "API 经 dev 代理 /api -> localhost:8001"
echo ""

pnpm dev:h5 --host 127.0.0.1 --port "$PORT"
