# Collection do Postman

Todas as chamadas da demonstração, já na ordem do vídeo e com testes em cada requisição.

| Arquivo | Para que serve |
|---|---|
| `vehicle-platform.postman_collection.json` | A collection: 8 pastas e 26 requisições |
| `kubernetes-port-forward.postman_environment.json` | Environment para o cluster (`localhost:18081` / `localhost:18082`) |
| `local-docker-compose.postman_environment.json` | Environment para o `docker compose` (`localhost:8081` / `localhost:8082`) |

## Como usar

1. No Postman, clique em **Import** e arraste os três arquivos.
2. Escolha o environment no canto superior direito.
3. **Kubernetes:** deixe os serviços acessíveis em `localhost` e preencha `webhook_secret` no environment com o valor do secret `WEBHOOK_SECRET`.

   ```bash
   KUBE_CONTEXT=minikube ./scripts/port-forward.sh
   ```

   O valor do secret foi mostrado pelo `configurar-secrets.sh`. Também dá para ler no cluster:

   ```bash
   kubectl --context minikube -n vehicle-platform get secret vehicle-sales-service-secrets -o jsonpath='{.data.WEBHOOK_SECRET}' | base64 -d
   ```

4. Rode as pastas em ordem, uma requisição por vez, para narrar no vídeo. Ou use **Run collection**: o Runner mostra todas as chamadas e os testes passando.

Os ids dos veículos, o código de pagamento e a data da venda (hoje, no fuso de São Paulo) são preenchidos sozinhos nas variáveis da collection. Cada execução cadastra veículos novos, então a collection pode ser rodada quantas vezes quiser.

## Roteiro

| Pasta | O que mostra |
|---|---|
| 0. Saúde dos serviços | Os dois serviços no ar (probes de readiness) |
| 1. Cadastro de veículos | Cadastro do Corolla e do Onix no serviço principal, mais consulta e listagem |
| 2. Vitrine | Os dois já aparecem no serviço de venda (comunicação HTTP), do mais barato para o mais caro |
| 3. Edição | Edição do preço no principal chegando ao serviço de venda |
| 4. Compra | Compra com CPF e data da venda; o veículo sai da vitrine e a edição dele passa a dar `409` |
| 5. Efetivação pelo webhook | Webhook `PAID` → venda efetivada → lista de vendidos com CPF mascarado |
| 6. Cancelamento | Webhook `CANCELED` → o veículo volta à vitrine |
| 7. Regras e segurança | Webhook idempotente e contraditório, falta de segredo, CPF inválido, data futura, validação, endpoint interno protegido |
