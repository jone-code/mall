#!/usr/bin/env bash
# 启动 product-service (:8103)，商品/类目管理、Dashboard 商品统计依赖此服务
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export MYSQL_HOST="${MYSQL_HOST:-localhost}"
export MYSQL_PORT="${MYSQL_PORT:-13306}"
# shellcheck source=lib/java-service.sh
source "$SCRIPT_DIR/lib/java-service.sh"
start_java_service_bg product-service "$SCRIPT_DIR/logs/product-service.log" "$SCRIPT_DIR/pids/product-service.pid"
