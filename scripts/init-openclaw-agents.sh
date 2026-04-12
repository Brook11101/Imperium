#!/usr/bin/env bash
set -euo pipefail

OPENCLAW_BIN="${OPENCLAW_PATH:-openclaw}"
STATE_DIR="${OPENCLAW_STATE_DIR:-$HOME/.openclaw}"

agents=(
  praeco
  senator_strategos
  senator_juris
  senator_fiscus
  tribune
  consul
  legatus
  praetor
  aedile
  quaestor
  scriba
  governor
  censor
)

for agent in "${agents[@]}"; do
  workspace="$STATE_DIR/workspace-$agent"
  if "$OPENCLAW_BIN" agents list --json | grep -q '"id": "'$agent'"'; then
    echo "[imperium] agent already exists: $agent"
    continue
  fi

  echo "[imperium] creating agent: $agent"
  "$OPENCLAW_BIN" agents add "$agent" --workspace "$workspace" --non-interactive --json >/dev/null
done

echo "[imperium] OpenClaw agents initialized"
