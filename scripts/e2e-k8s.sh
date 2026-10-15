#!/usr/bin/env bash
# Abre túneis (port-forward) para os dois serviços no Kubernetes e roda o teste ponta a ponta.
# Variáveis: WEBHOOK_SECRET (obrigatória), KUBE_CONTEXT (opcional, ex.: minikube)
set -euo pipefail

: "${WEBHOOK_SECRET:?defina WEBHOOK_SECRET}"
cd "$(dirname "$0")/.."

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} -n vehicle-platform "$@"; }

kube port-forward svc/vehicle-core-service 18081:80 > /dev/null 2>&1 &
CORE_PF=$!
kube port-forward svc/vehicle-sales-service 18082:80 > /dev/null 2>&1 &
SALES_PF=$!
trap 'kill "$CORE_PF" "$SALES_PF" 2> /dev/null || true' EXIT

for port in 18081 18082; do
  for _ in $(seq 1 30); do
    curl -fsS "http://localhost:$port/actuator/health/readiness" > /dev/null 2>&1 && break
    sleep 1
  done
done

CORE_URL=http://localhost:18081 SALES_URL=http://localhost:18082 bash scripts/e2e.sh
