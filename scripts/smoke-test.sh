#!/usr/bin/env bash
# Confere, depois do deploy, que o serviço responde dentro do cluster.
# Opcional: KUBE_CONTEXT (ex.: minikube), LOCAL_PORT (padrão 18081)
set -euo pipefail

NAMESPACE=vehicle-platform
SERVICE=vehicle-core-service
LOCAL_PORT="${LOCAL_PORT:-18081}"

kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} -n "$NAMESPACE" \
  port-forward "svc/$SERVICE" "$LOCAL_PORT:80" > /dev/null 2>&1 &
PORT_FORWARD_PID=$!
trap 'kill "$PORT_FORWARD_PID" 2> /dev/null || true' EXIT

for _ in $(seq 1 30); do
  if curl -fsS "http://localhost:$LOCAL_PORT/actuator/health/readiness"; then
    echo
    curl -fsS "http://localhost:$LOCAL_PORT/vehicles?size=1" > /dev/null
    echo "Smoke test OK: $SERVICE pronto e respondendo"
    exit 0
  fi
  sleep 2
done

echo "Smoke test FALHOU: $SERVICE não respondeu" >&2
exit 1
