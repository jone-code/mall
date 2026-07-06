#!/usr/bin/env bash
# 在宿主机本地后台启动三个 Java 服务
# 日志输出到 mall/deploy/logs/{service}.log
set -euo pipefail

cd "$(dirname "$0")/../backend"

mkdir -p ../deploy/logs ../deploy/pids

export MALL_JWT_SECRET="${MALL_JWT_SECRET:-dev_secret_please_change_to_a_long_random_string_32bytes}"
export MALL_ADMIN_JWT_SECRET="${MALL_ADMIN_JWT_SECRET:-dev_admin_secret_please_change_32bytes_long_random}"

run() {
  local mod="$1"
  local log="../deploy/logs/${mod}.log"
  local pid="../deploy/pids/${mod}.pid"
  if [ -f "$pid" ] && kill -0 "$(cat "$pid")" 2>/dev/null; then
    echo "[$mod] already running pid=$(cat "$pid")"
    return
  fi
  echo "[$mod] install dependencies..."
  mvn -pl "$mod" -am -DskipTests install -q
  echo "[$mod] starting -> $log"
  if command -v setsid >/dev/null 2>&1; then
    setsid nohup mvn -pl "$mod" spring-boot:run >> "$log" 2>&1 &
  else
    nohup mvn -pl "$mod" spring-boot:run >> "$log" 2>&1 &
    disown || true
  fi
  echo $! > "$pid"
}

CMD="${1:-up}"
case "$CMD" in
  up)
    run user-service
    run product-service
    run cart-service
    run order-service
    run pay-service
    run search-service
    run review-service
    run admin-service
    run mall-bff
    echo
    echo "=> 等待启动（约 20-40s 编译+起服务），可用 tail -f mall/deploy/logs/*.log 观察"
    ;;
  down)
    for f in ../deploy/pids/*.pid; do
      [ -f "$f" ] || continue
      pid=$(cat "$f")
      echo "[$(basename "$f" .pid)] kill $pid"
      kill "$pid" 2>/dev/null || true
      rm -f "$f"
    done
    # 兜底：杀掉 spring-boot 子进程
    pkill -f 'spring-boot:run' 2>/dev/null || true
    ;;
  status)
    for f in ../deploy/pids/*.pid; do
      [ -f "$f" ] || continue
      pid=$(cat "$f")
      name=$(basename "$f" .pid)
      if kill -0 "$pid" 2>/dev/null; then
        echo "[$name] running pid=$pid"
      else
        echo "[$name] dead (stale pidfile)"
      fi
    done
    ;;
  *)
    echo "用法: $0 {up|down|status}"
    exit 1
    ;;
esac
