#!/usr/bin/env bash
# Teste ponta a ponta da plataforma, contra os dois serviços rodando de verdade:
# cadastro -> vitrine -> edição -> compra -> webhook (pago) -> vendidos, e o caminho do cancelamento.
#
# Uso (docker compose):  ./scripts/e2e.sh
# Uso (Kubernetes):      CORE_URL=http://localhost:18081 SALES_URL=http://localhost:18082 \
#                        WEBHOOK_SECRET=<segredo> ./scripts/e2e.sh
set -euo pipefail

CORE_URL="${CORE_URL:-http://localhost:8081}"
SALES_URL="${SALES_URL:-http://localhost:8082}"
WEBHOOK_SECRET="${WEBHOOK_SECRET:-local-webhook-secret}"
BUYER_CPF="${BUYER_CPF:-529.982.247-25}"

BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT

step() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
ok() { printf '\033[0;32m    ok: %s\033[0m\n' "$*"; }
fail() { printf '\033[0;31m    FALHOU: %s\033[0m\n' "$*"; exit 1; }

# request MÉTODO URL [CORPO] [args extras do curl...]  ->  preenche STATUS e RESPONSE
request() {
  local method=$1 url=$2 body=${3:-}
  local args=(-sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$url" -H 'Content-Type: application/json')
  [[ $# -gt 3 ]] && args+=("${@:4}")
  [[ -n $body ]] && args+=(--data "$body")
  STATUS=$(curl "${args[@]}")
  RESPONSE=$(cat "$BODY_FILE")
  printf '    %s %s -> HTTP %s\n    %s\n' "$method" "$url" "$STATUS" "$RESPONSE"
}

expect_status() { [[ $STATUS == "$1" ]] && ok "HTTP $1" || fail "esperado HTTP $1, recebido $STATUS"; }
expect_contains() { [[ $RESPONSE == *"$1"* ]] && ok "$2" || fail "$2"; }
expect_absent() { [[ $RESPONSE != *"$1"* ]] && ok "$2" || fail "$2"; }
field() { sed -E "s/.*\"$1\":\"?([^\",}]*)\"?.*/\1/" <<<"$RESPONSE"; }

vehicle_json() { printf '{"brand":"%s","model":"%s","year":%s,"color":"%s","price":%s}' "$@"; }

step "1. Cadastro de dois veículos no serviço principal"
request POST "$CORE_URL/vehicles" "$(vehicle_json Toyota Corolla 2022 Prata 120000.00)"
expect_status 201
COROLLA=$(field id)
request POST "$CORE_URL/vehicles" "$(vehicle_json Chevrolet Onix 2023 Branco 85000.00)"
expect_status 201
ONIX=$(field id)

step "2. Os dois aparecem na vitrine do serviço de venda, do mais barato para o mais caro"
request GET "$SALES_URL/vehicles/available?size=100"
expect_status 200
expect_contains "$COROLLA" "Corolla sincronizado com o serviço de venda"
expect_contains "$ONIX" "Onix sincronizado com o serviço de venda"
before_onix=${RESPONSE%%"$ONIX"*}
before_corolla=${RESPONSE%%"$COROLLA"*}
(( ${#before_onix} < ${#before_corolla} )) && ok "Onix (R\$ 85 mil) vem antes do Corolla (R\$ 120 mil)" \
  || fail "ordem por preço incorreta"

step "3. Edição do preço no serviço principal chega ao serviço de venda"
request PUT "$CORE_URL/vehicles/$COROLLA" "$(vehicle_json Toyota Corolla 2022 Prata 118000.00)"
expect_status 200
request GET "$SALES_URL/vehicles/available?size=100"
expect_contains "118000" "novo preço visível na vitrine"

step "4. Compra do Corolla (CPF + data da venda)"
request POST "$SALES_URL/sales" "{\"vehicleId\":\"$COROLLA\",\"buyerCpf\":\"$BUYER_CPF\"}"
expect_status 201
expect_contains '"paymentStatus":"PENDING"' "venda aguardando pagamento"
PAYMENT_CODE=$(field paymentCode)
SALE_ID=$(field id)

step "5. Veículo reservado sai da vitrine e não pode mais ser editado"
request GET "$SALES_URL/vehicles/available?size=100"
expect_absent "$COROLLA" "Corolla fora da vitrine"
request PUT "$CORE_URL/vehicles/$COROLLA" "$(vehicle_json Toyota Corolla 2022 Prata 1.00)"
expect_status 409

step "6. Processador de pagamento confirma pelo webhook"
request POST "$SALES_URL/webhooks/payments" "{\"paymentCode\":\"$PAYMENT_CODE\",\"status\":\"PAID\"}" \
  -H "X-Webhook-Secret: $WEBHOOK_SECRET"
expect_status 200
expect_contains '"paymentStatus":"PAID"' "pagamento efetivado"
request GET "$SALES_URL/sales/$SALE_ID"
expect_contains '"paymentStatus":"PAID"' "venda consultável com status PAID"

step "7. Corolla aparece na lista de vendidos, com CPF mascarado"
request GET "$SALES_URL/vehicles/sold?size=100"
expect_status 200
expect_contains "$COROLLA" "Corolla na lista de vendidos"
expect_contains '***.982.247-**' "CPF mascarado"

step "8. Caminho do cancelamento: compra do Onix cancelada devolve o veículo à vitrine"
request POST "$SALES_URL/sales" "{\"vehicleId\":\"$ONIX\",\"buyerCpf\":\"$BUYER_CPF\"}"
expect_status 201
PAYMENT_CODE=$(field paymentCode)
request POST "$SALES_URL/webhooks/payments" "{\"paymentCode\":\"$PAYMENT_CODE\",\"status\":\"CANCELED\"}" \
  -H "X-Webhook-Secret: $WEBHOOK_SECRET"
expect_status 200
request GET "$SALES_URL/vehicles/available?size=100"
expect_contains "$ONIX" "Onix de volta à vitrine"

printf '\n\033[1;32mTeste ponta a ponta concluído com sucesso.\033[0m\n'
