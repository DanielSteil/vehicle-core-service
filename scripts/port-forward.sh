#!/usr/bin/env bash
# Expõe os dois serviços do Kubernetes em localhost, para o Postman e o navegador (Ctrl+C encerra).
#   serviço principal: http://localhost:18081    serviço de venda: http://localhost:18082
# Reconecta sozinho quando o pod é trocado (deploy durante a demonstração, restart do cluster).
# Opcional: KUBE_CONTEXT (ex.: minikube)
set -uo pipefail

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} -n vehicle-platform "$@"; }

forward() {
  local service=$1 port=$2
  while true; do
    kube port-forward "svc/$service" "$port:80" > /dev/null 2>&1
    sleep 1
  done
}

trap 'trap - EXIT INT TERM; kill 0 2> /dev/null' EXIT INT TERM
forward vehicle-core-service 18081 &
forward vehicle-sales-service 18082 &

echo "Serviço principal: http://localhost:18081  (Swagger: http://localhost:18081/swagger-ui.html)"
echo "Serviço de venda:  http://localhost:18082  (Swagger: http://localhost:18082/swagger-ui.html)"
echo "Ctrl+C para encerrar."
wait
