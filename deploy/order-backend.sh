#!/usr/bin/env bash
# 启动 order-service (:8105)，Dashboard / 订单管理依赖此服务
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=lib/java-service.sh
source "$SCRIPT_DIR/lib/java-service.sh"
start_java_service_bg order-service "$SCRIPT_DIR/logs/order-service.log" "$SCRIPT_DIR/pids/order-service.pid"
