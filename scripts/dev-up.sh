#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "[imperium] starting infrastructure + app stack"
docker compose up -d mysql rocketmq-namesrv rocketmq-broker rocketmq-dashboard imperium-api imperium-worker imperium-web

echo
echo "Frontend: http://localhost:5173"
echo "API:      http://localhost:8080"
echo "Swagger:  http://localhost:8080/swagger-ui.html"
echo "RocketMQ: http://localhost:8081"
