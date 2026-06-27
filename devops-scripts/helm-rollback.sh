#!/usr/bin/env bash
# helm-rollback.sh – Roll back a Helm release to the previous revision
set -euo pipefail

RELEASE="${1:-payg-payment-gateway}"
NAMESPACE="${2:-payg-production}"
REVISION="${3:-0}"  # 0 = previous revision

echo "⏪ Rolling back ${RELEASE} in namespace ${NAMESPACE}..."
if [[ "${REVISION}" -eq 0 ]]; then
  helm rollback "${RELEASE}" -n "${NAMESPACE}" --wait --timeout 5m
else
  helm rollback "${RELEASE}" "${REVISION}" -n "${NAMESPACE}" --wait --timeout 5m
fi

echo "✅ Rollback complete. Current release status:"
helm status "${RELEASE}" -n "${NAMESPACE}"
