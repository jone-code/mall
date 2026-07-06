#!/usr/bin/env bash
# C 端网关 mall-bff（端口 8001，前端 app 连这个）
# 前提：user-service 已在 8101 运行
set -euo pipefail

cd "$(dirname "$0")/../backend"

export MALL_JWT_SECRET="${MALL_JWT_SECRET:-dev_secret_please_change_to_a_long_random_string_32bytes}"
export USER_SERVICE_URL="${USER_SERVICE_URL:-http://localhost:8101}"

echo "启动 mall-bff -> http://localhost:8001"
echo "App H5 请求走 http://localhost:8001/api（或 dev 代理 /api）"
echo ""

mvn -pl mall-bff -am -DskipTests install
mvn -pl mall-bff spring-boot:run
