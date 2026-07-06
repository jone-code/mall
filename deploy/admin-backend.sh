#!/usr/bin/env bash
# 只启动管理端后端 admin-service（端口 8102）
# 前提：MySQL :13306、Redis :16379 已启动（见下方说明）
set -euo pipefail

cd "$(dirname "$0")/../backend"

export MALL_ADMIN_JWT_SECRET="${MALL_ADMIN_JWT_SECRET:-dev_admin_secret_please_change_32bytes_long_random}"

echo "启动 admin-service -> http://localhost:8102"
echo "短信验证码会打在下方控制台（STUB 模式）"
echo ""

# spring-boot:run 不能直接和 -am 一起跑：Maven 会尝试在父聚合工程 mall-backend 上找 main class。
# 先把依赖模块装到本地仓库，再只对 admin-service 执行 Spring Boot 启动。
mvn -pl admin-service -am -DskipTests install
mvn -pl admin-service spring-boot:run
