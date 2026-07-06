#!/usr/bin/env bash
# 中间件一键启停 (mysql + redis)
# Java 服务在宿主机本地启动，见 ../README.md 或 run-services.sh
set -euo pipefail

cd "$(dirname "$0")"

CMD="${1:-up}"

case "$CMD" in
  up)
    echo "==> 启动 mysql / redis"
    docker compose up -d
    sleep 3
    docker compose ps
    cat <<'EOF'

中间件就绪:
  - MySQL: localhost:13306  (root/root, db: mall)
  - Redis: localhost:16379

接下来在宿主机本地启动 Java 服务（三个终端各一）:

  cd mall/backend
  export MALL_JWT_SECRET=dev_secret_please_change_to_a_long_random_string_32bytes
  export MALL_ADMIN_JWT_SECRET=dev_admin_secret_please_change_32bytes_long_random
  mvn -pl user-service  -am spring-boot:run    # :8101
  mvn -pl admin-service -am spring-boot:run    # :8102
  mvn -pl mall-bff      -am spring-boot:run    # :8001

或一键全部后台启动: ./run-services.sh
EOF
    ;;
  logs)
    docker compose logs -f --tail=100
    ;;
  down)
    docker compose down
    ;;
  clean)
    echo "==> 停止容器并删除数据卷（mall_user/mall_admin 旧数据会一并清掉）"
    docker compose down -v
  ;;
  reset)
    echo "==> 完全重置 MySQL/Redis（删容器 + 数据卷，按 mysql-init.sql 重建 mall 库）"
    docker compose down -v
    docker compose up -d
    sleep 3
    docker compose ps
    cat <<'EOF'

已用单库 mall 重新初始化。若 Java 服务仍在跑，请先:

  cd mall/deploy && ./run-services.sh down && ./run-services.sh up

EOF
    ;;
  ps)
    docker compose ps
    ;;
  *)
    echo "用法: $0 {up|logs|down|clean|reset|ps}"
    exit 1
    ;;
esac
