#!/usr/bin/env bash
# C 端用户服务 user-service（端口 8101）
# 前提：MySQL :13306、Redis :16379 已启动
set -euo pipefail

cd "$(dirname "$0")/../backend"

export MALL_JWT_SECRET="${MALL_JWT_SECRET:-dev_secret_please_change_to_a_long_random_string_32bytes}"

echo "启动 user-service -> http://localhost:8101"
echo "短信验证码会打在下方控制台：【STUB SMS】phone=... code=..."
echo ""

mvn -pl user-service -am -DskipTests install
mvn -pl user-service spring-boot:run
