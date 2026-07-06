#!/usr/bin/env bash
# 只启动管理端前端 admin-web（默认 http://localhost:5173）
# 前提：admin-service 已在 8102 运行
set -euo pipefail

cd "$(dirname "$0")/../frontend/admin-web"

PORT="${ADMIN_WEB_PORT:-5173}"

if [ ! -d node_modules ]; then
  echo "首次运行，安装依赖..."
  pnpm install
fi

echo "启动 admin-web -> http://localhost:$PORT"
echo "默认账号: admin / Admin@12345"
echo ""

pnpm dev --host 127.0.0.1 --port "$PORT"
