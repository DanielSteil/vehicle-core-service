# Roteiro do vídeo de demonstração

Duração sugerida: 12 a 15 minutos. O enunciado pede para mostrar a solução **funcionando na infraestrutura e no uso**, com teste ponta a ponta (cadastro, compra e efetivação), os serviços **segregados mas se comunicando**, e os **testes rodando com evidência de cobertura**.

## Antes de gravar

```bash
minikube start
minikube addons enable metrics-server
kubectl -n vehicle-platform get pods        # 5 pods Running
```

Deixe abertos: os dois repositórios no GitHub (aba Actions), um terminal na pasta `vehicle-core-service` e outro na `vehicle-sales-service`.

## 1. Arquitetura (2 min)

- Abra `docs/arquitetura.md` no GitHub e mostre o diagrama: dois serviços, dois bancos e a chamada HTTP só do principal para o de venda.
- Explique o porquê:
  - listagens e compra isoladas, com uma cópia local dos dados, para aguentar picos (HPA);
  - o serviço de venda decide o status do veículo;
  - o webhook fica no serviço de venda.
- Mostre a tabela "Campos que o enunciado não trazia".

## 2. Infraestrutura segregada (2 min)

```bash
kubectl -n vehicle-platform get deploy,statefulset,svc,hpa,pvc
kubectl -n vehicle-platform get configmap vehicle-core-service-config -o yaml   # SALES_SERVICE_URL = DNS interno
```

- Aponte: `core-db` e `sales-db` são **StatefulSets diferentes, com PVCs diferentes**.
- Mostre `k8s/deployment.yaml` e `k8s/service.yaml` do repositório principal (Deployments e Services pedidos no enunciado).

## 3. CI/CD com Pull Request (3 min)

1. Crie uma branch, faça uma mudança pequena (por exemplo, no README) e abra um PR.
2. Mostre o workflow **CI** rodando no PR: testes unitários e de integração (Testcontainers) e a trava de 80%.
3. Abra o **Summary** da execução: a tabela de cobertura (linhas, branches e instruções) e o artefato `jacoco-report`.
4. Faça o merge e mostre o **CD** disparado pelo merge:
   - testes;
   - imagem publicada no GHCR com a tag do commit;
   - deploy no runner auto-hospedado;
   - smoke test.
5. No terminal, `kubectl -n vehicle-platform get pods -w` mostrando o rolling update.

## 4. Testes e cobertura localmente (1 min)

```bash
mvn verify            # nos dois repositórios
```

Abra `target/site/jacoco/index.html`: sales com cerca de 99% de linhas, core com cerca de 98%.

## 5. Teste ponta a ponta no cluster (3 min)

Rode no terminal (ou pelo workflow **E2E no cluster**, na aba Actions do repositório principal):

```bash
WEBHOOK_SECRET=<segredo> KUBE_CONTEXT=minikube ./scripts/e2e-k8s.sh
```

Narre cada etapa conforme ela aparece:

1. Cadastro de dois veículos **no serviço principal**.
2. Os dois aparecem na vitrine **do serviço de venda**: é a comunicação HTTP entre os serviços. Ordem por preço.
3. Edição do preço chega ao serviço de venda.
4. Compra do Corolla com CPF: venda `PENDING` e `paymentCode`.
5. O Corolla sai da vitrine e a edição dele passa a dar `409`: o serviço de venda é quem decide.
6. Webhook `PAID`: efetivação da compra.
7. Corolla na lista de vendidos, com o CPF mascarado.
8. Compra do Onix cancelada pelo webhook: o carro volta à vitrine.

Opcional: mostre os dois bancos separados.

```bash
kubectl -n vehicle-platform exec core-db-0 -- psql -U core -d core_db -c "select brand, model, price from vehicles"
kubectl -n vehicle-platform exec sales-db-0 -- psql -U sales -d sales_db -c "select model, status from vehicles"
kubectl -n vehicle-platform exec sales-db-0 -- psql -U sales -d sales_db -c "select payment_status, price from sales"
```

## 6. Aumento repentino de chamadas (2 min)

No repositório do serviço de venda:

```bash
KUBE_CONTEXT=minikube ./scripts/load-test.sh 6 120
```

Mostre o HPA levando as réplicas de 2 para 4 ou mais enquanto a CPU passa de 70%.

## 7. Fechamento (30 s)

Recapitule os entregáveis:

- 2 repositórios com README;
- banco segregado em cada serviço;
- CI/CD por PR, com deploy no merge;
- cobertura acima de 80%;
- teste ponta a ponta funcionando.
