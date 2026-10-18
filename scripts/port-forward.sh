#!/usr/bin/env bash
# Expõe os dois serviços do Kubernetes em localhost, para o Postman e o navegador (Ctrl+C encerra).
#   serviço principal: http://localhost:18081    serviço de venda: http://localhost:18082
# Opcional: KUBE_CONTEXT (ex.: minikube)
set -euo pipefail

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} -n vehicle-platform "$@"; }

kube port-forward svc/vehicle-core-service 18081:80 > /dev/null &
CORE_PF=$!
kube port-forward svc/vehicle-sales-service 18082:80 > /dev/null &
SALES_PF=$!
trap 'kill "$CORE_PF" "$SALES_PF" 2> /dev/null || true' EXIT INT TERM

echo "Serviço principal: http://localhost:18081  (Swagger: http://localhost:18081/swagger-ui.html)"
echo "Serviço de venda:  http://localhost:18082  (Swagger: http://localhost:18082/swagger-ui.html)"
echo "Ctrl+C para encerrar."
wait
