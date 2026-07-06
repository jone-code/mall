#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=lib/java-service.sh
source "$SCRIPT_DIR/lib/java-service.sh"
start_java_service_bg cart-service "$SCRIPT_DIR/logs/cart-service.log" "$SCRIPT_DIR/pids/cart-service.pid"
