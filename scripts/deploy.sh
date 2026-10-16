#!/usr/bin/env bash
# Publica o serviço principal no Kubernetes. Usado pelo pipeline de CD e para deploy manual.
#
# Variáveis obrigatórias: IMAGE, CORE_DB_PASSWORD, INTERNAL_API_TOKEN
# Opcional: KUBE_CONTEXT (ex.: minikube)
set -euo pipefail

: "${IMAGE:?defina IMAGE (ex.: ghcr.io/<dono>/vehicle-core-service:<tag>)}"
: "${CORE_DB_PASSWORD:?defina CORE_DB_PASSWORD}"
: "${INTERNAL_API_TOKEN:?defina INTERNAL_API_TOKEN}"

NAMESPACE=vehicle-platform
cd "$(dirname "$0")/.."

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} "$@"; }
apply_secret() {
  local name=$1
  shift
  kube -n "$NAMESPACE" create secret generic "$name" "$@" --dry-run=client -o yaml | kube apply -f -
}

echo "==> Namespace e segredos"
kube apply -f k8s/namespace.yaml
apply_secret core-db-credentials \
  --from-literal=username=core \
  --from-literal=password="$CORE_DB_PASSWORD"
apply_secret vehicle-core-service-secrets \
  --from-literal=SALES_INTERNAL_TOKEN="$INTERNAL_API_TOKEN"

echo "==> Manifests (imagem: $IMAGE)"
SECRETS_HASH=$(printf '%s|%s' "$CORE_DB_PASSWORD" "$INTERNAL_API_TOKEN" | sha256sum | cut -c1-16)
kube kustomize k8s \
  | sed -E -e "s#^([[:space:]]*image:[[:space:]]*)vehicle-core-service\$#\1${IMAGE}#" \
           -e "s#^([[:space:]]*vehicle-platform/secrets-hash:[[:space:]]*).*#\1\"${SECRETS_HASH}\"#" \
  | kube apply -f -

echo "==> Aguardando o rollout"
kube -n "$NAMESPACE" rollout status statefulset/core-db --timeout=180s
kube -n "$NAMESPACE" rollout status deployment/vehicle-core-service --timeout=300s
kube -n "$NAMESPACE" get pods -l app.kubernetes.io/part-of=vehicle-platform -o wide
