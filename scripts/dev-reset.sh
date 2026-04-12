#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

docker compose down -v
rm -rf frontend/node_modules frontend/dist backend/*/target

echo "[imperium] local containers, volumes, and build artifacts removed"
