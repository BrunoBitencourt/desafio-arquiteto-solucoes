# Controle de Fluxo de Caixa

Solução desenvolvida como desafio técnico para demonstrar uma arquitetura baseada em microsserviços, utilizando Java, Spring Boot, PostgreSQL e RabbitMQ.

## 1. Visão geral

A solução permite registrar lançamentos financeiros (créditos e débitos) e consultar o saldo consolidado por dia.

A arquitetura é composta por dois microsserviços:

- **lancamentos-service:** responsável pelo registro e publicação dos lançamentos.
- **consolidacao_service:** responsável pelo consumo das mensagens, persistência e consolidação diária.

A comunicação entre os serviços é assíncrona, utilizando RabbitMQ, evitando o acoplamento direto entre o registro de lançamentos e o processamento da consolidação.

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
- PostgreSQL
- RabbitMQ
- Maven
- Flyway

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
├── .gitignore
└── README.md
```

## 5. Pré-requisitos

Para executar a aplicação localmente, é necessário ter instalado:

- Java 21
- PostgreSQL
- RabbitMQ

Também é necessário:

- Criar os bancos de dados `lancamentos_db` e `consolidacao_db`.
- Configurar um usuário com permissões nos dois bancos.
- Configurar as credenciais de acesso ao RabbitMQ.
- Garantir que PostgreSQL e RabbitMQ estejam em execução.

As migrations do Flyway são executadas na inicialização dos serviços.

**Importante:** as credenciais são fornecidas por variáveis de ambiente. Não armazene senhas reais no repositório.

## 6. Executando a aplicação no Windows

Os comandos abaixo utilizam o PowerShell.

Os dois microsserviços devem ser executados em terminais separados.

### 6.1. Iniciar o serviço de lançamentos

Abra um terminal PowerShell e acesse a pasta:

```powershell
cd C:\Teste_Projeto\lancamentos-service
```

Configure as variáveis de ambiente:

```powershell
$env:APP_USER = "desafio"
$env:APP_PASSWORD = "SUA_SENHA_LOCAL"

$env:DB_USERNAME = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA_DO_BANCO"
$env:DB_URL = "jdbc:postgresql://localhost:5433/lancamentos_db"

$env:RABBITMQ_USERNAME = "SEU_USUARIO_RABBITMQ"
$env:RABBITMQ_PASSWORD = "SUA_SENHA_RABBITMQ"
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
```

Execute o serviço:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta **8080**.

### 6.2. Iniciar o serviço de consolidação

Abra outro terminal PowerShell e acesse:

```powershell
cd C:\Teste_Projeto\consolidacao_service
```

Configure as variáveis de ambiente:

```powershell
$env:DB_USERNAME = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA_DO_BANCO"
$env:DB_URL = "jdbc:postgresql://localhost:5433/consolidacao_db"

$env:RABBITMQ_USERNAME = "SEU_USUARIO_RABBITMQ"
$env:RABBITMQ_PASSWORD = "SUA_SENHA_RABBITMQ"
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
```

Execute o serviço:

```powershell
.\mvnw spring-boot:run
```

Serviço disponível na porta **8181**.

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

Também está disponível o endpoint:

```http
GET http://localhost:8181/api/lancamentos-recebidos
```

Para consultar os lançamentos recebidos pelo serviço de consolidação.

## 8. Resiliência e
