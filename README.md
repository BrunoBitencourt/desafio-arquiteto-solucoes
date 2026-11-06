# Controle de Fluxo de Caixa

Solução desenvolvida como desafio técnico para demonstrar uma arquitetura baseada em microsserviços, utilizando Java 21, Spring Boot, PostgreSQL e RabbitMQ.

## 1. Visão geral

A solução permite registrar lançamentos financeiros (créditos e débitos) e consultar o saldo consolidado por dia.

A arquitetura é composta por dois microsserviços:

- **lancamentos-service:** responsável pelo registro e publicação dos lançamentos.
- **consolidacao_service:** responsável pelo consumo das mensagens, persistência e consolidação diária.

A comunicação entre os serviços é assíncrona, utilizando RabbitMQ, reduzindo o acoplamento entre o registro de lançamentos e o processamento da consolidação.

## 2. Arquitetura

A solução utiliza uma arquitetura orientada a eventos (Event-Driven Architecture).

O fluxo funciona da seguinte forma:

1. O cliente envia um lançamento para o `lancamentos-service`.
2. O serviço registra o lançamento no PostgreSQL.
3. Uma mensagem é publicada no RabbitMQ.
4. O `consolidacao_service` consome a mensagem.
5. O lançamento recebido é validado e persistido.
6. A API de consolidação disponibiliza o resultado diário.

Documentação arquitetural:

- `docs/arquitetura.md`: requisitos, domínios funcionais e decisões arquiteturais.
- `docs/arquitetura.mmd`: diagrama da solução.

## 3. Tecnologias utilizadas

- Java 21
- Spring Boot 4.0.8
- Spring Data JPA
- Spring Security
- PostgreSQL 16
- RabbitMQ
- Maven
- Flyway
- k6

## 4. Estrutura do projeto

```text
Teste_Projeto/
├── lancamentos-service/
│   └── src/
├── consolidacao_service/
│   └── src/
├── docs/
│   ├── arquitetura.md
│   └── arquitetura.mmd
├── tests/
│   └── load/
│       └── lancamentos-load-test.js
├── .gitignore
└── README.md
```

## 5. Pré-requisitos

Para executar a aplicação localmente, é necessário ter instalado:

- Java 21
- PostgreSQL
- RabbitMQ

Para executar os testes de carga, também é necessário instalar o k6.

É necessário preparar:

- Banco de dados `lancamentos_db`.
- Banco de dados `consolidacao_db`.
- Usuário PostgreSQL com permissões nos dois bancos.
- Usuário e senha de acesso ao RabbitMQ.
- RabbitMQ e PostgreSQL em execução.

As migrations do Flyway são executadas automaticamente na inicialização dos serviços.

**Importante:** as credenciais são fornecidas por variáveis de ambiente. Não armazene senhas reais no repositório.

## 6. Executando a aplicação no Windows

Os comandos abaixo utilizam PowerShell.

Os dois microsserviços devem ser executados em terminais separados.

### 6.1. Serviço de lançamentos

Abra um terminal PowerShell:

```powershell
cd C:\Teste_Projeto\lancamentos-service
```

Configure as variáveis de ambiente:

```powershell
$env:APP_USER = "desafio"
$env:APP_PASSWORD = "SUA_SENHA"

$env:DB_USERNAME = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA_DO_BANCO"
$env:DB_URL = "jdbc:postgresql://localhost:5433/lancamentos_db"

$env:RABBITMQ_USERNAME = "SEU_USUARIO_RABBITMQ"
$env:RABBITMQ_PASSWORD = "SUA_SENHA_RABBITMQ"
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
```

Execute:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta **8080**.

### 6.2. Serviço de consolidação

Abra outro terminal PowerShell:

```powershell
cd C:\Teste_Projeto\consolidacao_service
```

Configure as variáveis de ambiente:

```powershell
$env:APP_USER = "desafio"
$env:APP_PASSWORD = "SUA_SENHA"

$env:DB_USERNAME = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA_DO_BANCO"
$env:DB_URL = "jdbc:postgresql://localhost:5433/consolidacao_db"

$env:RABBITMQ_USERNAME = "SEU_USUARIO_RABBITMQ"
$env:RABBITMQ_PASSWORD = "SUA_SENHA_RABBITMQ"
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
```

Execute:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta **8181**.

## 7. APIs disponíveis

As APIs utilizam autenticação HTTP Basic.

O endpoint de health check é público. Os demais endpoints exigem autenticação.

### 7.1. Registrar lançamento

**POST**

```http
http://localhost:8080/api/lancamentos
```

Exemplo utilizando PowerShell:

```powershell
$body = @{
    valor = 150.00
    tipo = "CREDITO"
    dataLancamento = "2026-11-06"
    descricao = "Venda realizada"
} | ConvertTo-Json

$pair = "desafio:SUA_SENHA"
$encoded = [Convert]::ToBase64String(
    [Text.Encoding]::ASCII.GetBytes($pair)
)

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/lancamentos" `
    -Method Post `
    -Headers @{ Authorization = "Basic $encoded" } `
    -ContentType "application/json" `
    -Body $body
```

Valores aceitos para o tipo:

- `CREDITO`
- `DEBITO`

O serviço retorna HTTP 201 quando o lançamento é criado.

### 7.2. Consultar lançamentos

**GET**

```http
http://localhost:8080/api/lancamentos
```

Retorna os lançamentos registrados no serviço de origem.

### 7.3. Consultar lançamentos recebidos

**GET**

```http
http://localhost:8181/api/lancamentos-recebidos
```

Retorna os lançamentos processados pelo serviço de consolidação.

### 7.4. Consultar consolidado diário

**GET**

```http
http://localhost:8181/api/lancamentos-recebidos/consolidado?data=2026-11-06
```

Exemplo de resposta:

```json
{
  "data": "2026-11-06",
  "totalCreditos": 150.00,
  "totalDebitos": 73.00,
  "saldo": 77.00
}
```

O saldo é calculado pela fórmula:

**Saldo = Total de créditos - Total de débitos**

### 7.5. Health check

Os dois serviços disponibilizam o endpoint:

```http
GET /actuator/health
```

Também estão configurados endpoints de informação e métricas do Spring Boot Actuator.

## 8. Resiliência e confiabilidade

A solução contempla:

- Comunicação assíncrona via RabbitMQ.
- Retentativas no processamento de mensagens.
- Dead Letter Queue (DLQ).
- Validação de mensagens recebidas.
- Idempotência no recebimento de lançamentos.
- Bancos de dados separados por microsserviço.

O consumidor possui política de retentativas com até duas novas tentativas, intervalo inicial de um segundo, multiplicador de dois e intervalo máximo de cinco segundos.

Mensagens inválidas são excluídas das retentativas.

A separação entre os serviços permite que o registro de lançamentos continue funcionando mesmo quando o serviço de consolidação apresenta falhas, desde que o banco de lançamentos e a infraestrutura de mensageria estejam disponíveis.

## 9. Segurança

A solução utiliza Spring Security com autenticação HTTP Basic.

Critérios implementados:

- Autenticação obrigatória nas APIs, exceto health check.
- Usuário e senha fornecidos por variáveis de ambiente.
- Senhas codificadas pelo PasswordEncoder do Spring Security.
- Credenciais não armazenadas diretamente no código-fonte.
- Credenciais de PostgreSQL e RabbitMQ externalizadas.

Para produção, recomenda-se complementar a solução com HTTPS/TLS, gerenciamento seguro de segredos e mecanismos de autenticação mais robustos, conforme os requisitos do ambiente.

## 10. Testes automatizados

Para executar os testes do serviço de lançamentos:

```powershell
cd C:\Teste_Projeto\lancamentos-service
.\mvnw clean test
```

Para executar os testes do serviço de consolidação:

```powershell
cd C:\Teste_Projeto\consolidacao_service
.\mvnw clean test
```

Os testes de consolidação validam:

- Cálculo correto dos totais diários.
- Saldo negativo quando os débitos superam os créditos.
- Retorno de valores zerados quando não existem lançamentos.

## 11. Testes de carga

Foi utilizado o k6 para avaliar o comportamento do serviço de lançamentos sob carga.

Cenário configurado:

- Executor: `constant-arrival-rate`.
- Taxa configurada: 50 iterações por segundo.
- Duração: 1 minuto.
- Endpoint: `POST /api/lancamentos`.
- Validação: resposta HTTP 201.
- Limite de falhas HTTP: inferior a 5%.

Resultados obtidos:

| Indicador | Teste 1 | Teste 2 |
|---|---:|---:|
| Requisições concluídas | 2.958 | 2.956 |
| Vazão média | 48,91 req/s | 48,75 req/s |
| Falhas HTTP | 0% | 0% |
| Iterações descartadas | 43 | 33 |
| P95 | 1,22 s | 2,31 s |

Os testes demonstraram capacidade próxima de 50 requisições por segundo, sem falhas HTTP nas requisições concluídas.

Entretanto, houve iterações descartadas pelo k6. Portanto, o requisito de 50 requisições por segundo com perda máxima de 5% ainda não foi integralmente comprovado.

O arquivo de teste está disponível em:

`tests/load/lancamentos-load-test.js`

## 12. Limitações e evoluções futuras

- Implementação do padrão Transactional Outbox para garantir maior confiabilidade entre persistência e publicação de eventos.
- Alta disponibilidade do banco de dados e da mensageria.
- Evolução da autenticação e autorização para um mecanismo mais robusto.
- Validação definitiva do requisito de desempenho em infraestrutura dimensionada para produção.
- Monitoramento avançado com Prometheus e Grafana.
- Estratégia operacional de reprocessamento da DLQ.

## 13. Documentação

Consulte a pasta `docs` para conhecer:

- Domínios funcionais.
- Requisitos funcionais e não funcionais.
- Decisões arquiteturais.
- Estratégia de resiliência.
- Observabilidade.
- Estimativa de infraestrutura e custos AWS.
- Diagrama da solução.

A documentação arquitetural detalhada está disponível em `docs/arquitetura.md`.
