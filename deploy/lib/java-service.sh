#!/usr/bin/env bash
# 公共：后台启动单个 Spring Boot 模块（避免 -am + spring-boot:run 在父 POM 上报错）
# 用法: start_java_service_bg <module> <log-file> <pid-file>
start_java_service_bg() {
  local mod="$1"
  local log="$2"
  local pidfile="$3"
  cd "$(dirname "${BASH_SOURCE[0]}")/../../backend"
  mkdir -p ../deploy/logs ../deploy/pids
  if [ -f "$pidfile" ] && kill -0 "$(cat "$pidfile")" 2>/dev/null; then
    echo "[$mod] already running pid=$(cat "$pidfile")"
    return 0
  fi
  echo "[$mod] install dependencies..."
  mvn -pl "$mod" -am -DskipTests install -q
  echo "[$mod] starting -> $log"
  # setsid 脱离终端，避免脚本退出后子进程被回收
  if command -v setsid >/dev/null 2>&1; then
    setsid nohup mvn -pl "$mod" spring-boot:run >> "$log" 2>&1 &
  else
    nohup mvn -pl "$mod" spring-boot:run >> "$log" 2>&1 &
    disown || true
  fi
  echo $! > "$pidfile"
}
