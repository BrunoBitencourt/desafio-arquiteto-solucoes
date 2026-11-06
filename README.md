
# Controle de Fluxo de Caixa

Solução desenvolvida como desafio técnico para demonstrar uma arquitetura baseada em microsserviços, utilizando Java, Spring Boot, PostgreSQL e RabbitMQ.

## 1. Visão geral

A solução permite registrar lançamentos financeiros (créditos e débitos) e consultar o saldo consolidado por dia.

A arquitetura é composta por dois microsserviços:

- **lancamentos-service:** responsável pelo registro e publicação dos lançamentos.
- **consolidacao-service:** responsável pelo consumo das mensagens, persistência e consolidação diária.

## 2. Arquitetura

A comunicação entre os serviços utiliza RabbitMQ, seguindo o padrão de arquitetura orientada a eventos (Event-Driven Architecture).

O diagrama da solução está disponível em:

`docs/arquitetura.mmd`

A documentação arquitetural completa está em:

`docs/arquitetura.md`

## 3. Tecnologias utilizadas

- Java 21
- Spring Boot 4.0.8
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Maven

## 4. Estrutura do projeto

```text
Teste_Projeto/
├── lancamentos-service/
├── consolidacao_service/
├── docs/
│   ├── arquitetura.md
│   └── arquitetura.mmd
└── README.md
```

## 5. Pré-requisitos

Para executar localmente, é necessário ter instalado:

- Java 21
- PostgreSQL
- RabbitMQ

Também é necessário configurar os bancos de dados e as credenciais de conexão nos arquivos de configuração de cada serviço.

Não utilize credenciais reais ou senhas pessoais no repositório público.

## 6. Executando a aplicação

### 6.1. Iniciar o RabbitMQ

Certifique-se de que o RabbitMQ esteja em execução e que a fila `lancamentos.criados` esteja configurada.

A fila de mensagens não processadas é:

`lancamentos.criados.dlq`

### 6.2. Iniciar o serviço de lançamentos

No terminal:

```bash
cd lancamentos-service
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta 8080.

### 6.3. Iniciar o serviço de consolidação

Em outro terminal:

```bash
cd consolidacao_service
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta 8181.

## 7. Consulta do consolidado diário

Endpoint:

```http
GET http://localhost:8181/api/lancamentos-recebidos/consolidado?data=2026-10-22
```

Exemplo de resposta:

```json
{
  "data": "2026-10-22",
  "totalCreditos": 150.00,
  "totalDebitos": 73.00,
  "saldo": 77.00
}
```

O saldo é calculado pela fórmula:

**Saldo = Total de créditos - Total de débitos**

## 8. Resiliência e confiabilidade

A solução contempla:

- Comunicação assíncrona via RabbitMQ.
- Retentativas no processamento de mensagens.
- Dead Letter Queue (DLQ).
- Validação de mensagens.
- Idempotência no recebimento de lançamentos.

## 9. Testes

Para executar os testes automatizados do serviço de consolidação:

```powershell
cd consolidacao_service
.\mvnw test
```

Os testes validam:

- Cálculo correto do consolidado diário.
- Saldo negativo quando os débitos superam os créditos.
- Retorno de valores zerados quando não existem lançamentos.

## 10. Evoluções futuras

- Implementação do padrão Outbox.
- Autenticação e autorização das APIs.
- Alta disponibilidade de banco de dados e mensageria.
- Testes de carga para validar 50 requisições por segundo.
- Monitoramento com Prometheus e Grafana.
- Estratégia de reprocessamento da DLQ.

## 11. Documentação

Consulte a pasta `docs` para conhecer os domínios funcionais, requisitos, decisões arquiteturais e desenho da solução.
