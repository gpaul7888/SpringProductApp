#!/bin/bash
# Starts DB (per DB_MODE in .env), all services, and the frontend. Ctrl+C stops everything.
set -e
cd "$(dirname "$0")"
[ -f .env ] || cp .env.example .env
set -a; . ./.env; set +a
source ./env.sh >/dev/null

case "$DB_MODE" in
  docker)
    echo "DB_MODE=docker -> starting PostgreSQL container"
    docker compose up -d ;;
  postgres)
    echo "DB_MODE=postgres -> using local PostgreSQL" ;;
  *) echo "Unknown DB_MODE='$DB_MODE' in .env (use postgres or docker)"; exit 1 ;;
esac

echo -n "Waiting for PostgreSQL on localhost:5432"
for i in $(seq 1 30); do (echo > /dev/tcp/localhost/5432) 2>/dev/null && break; echo -n .; sleep 1; done
(echo > /dev/tcp/localhost/5432) 2>/dev/null || { echo " not reachable. Is PostgreSQL running?"; exit 1; }
echo " OK"

mkdir -p logs
JVM="-Dspring-boot.run.jvmArguments=-Xmx256m"
pids=()
trap 'echo; echo Stopping...; kill "${pids[@]}" 2>/dev/null; exit' INT TERM

start() { echo "Starting $1 (log: logs/$1.log)"; ./mvnw -q -pl "$1" spring-boot:run "$JVM" > "logs/$1.log" 2>&1 & pids+=($!); }
wait_port() { echo -n "  waiting for port $1"; for i in $(seq 1 120); do (echo > /dev/tcp/localhost/$1) 2>/dev/null && { echo " up"; return; }; echo -n .; sleep 2; done; echo " timeout (see logs/)"; }

start discovery-server; wait_port 8761
start auth-service
start product-service
start api-gateway;      wait_port 8090

echo "Starting frontend (log: logs/frontend.log)"
(cd frontend && npm start > ../logs/frontend.log 2>&1) & pids+=($!)
echo "All started. Open http://localhost:4200  —  Ctrl+C to stop all."
wait
