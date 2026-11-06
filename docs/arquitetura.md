
# Arquitetura da Solução — Controle de Fluxo de Caixa

## 1. Objetivo

Desenvolver uma solução para permitir que comerciantes controlem seus lançamentos financeiros diários, registrando créditos e débitos e consultando o saldo consolidado por data.

A solução foi dividida em dois serviços independentes:

- **lancamentos-service:** responsável pelo registro e controle dos lançamentos.
- **consolidacao-service:** responsável pelo recebimento dos lançamentos e cálculo do consolidado diário.

## 2. Domínios funcionais e capacidades de negócio

### Domínio: Gestão de Lançamentos

Capacidades:
- Registrar lançamentos financeiros.
- Classificar lançamentos como crédito ou débito.
- Consultar lançamentos.
- Publicar eventos de novos lançamentos.

### Domínio: Consolidação Financeira

Capacidades:
- Receber lançamentos publicados.
- Persistir os lançamentos recebidos.
- Consolidar créditos e débitos por data.
- Disponibilizar o saldo diário.

## 3. Requisitos funcionais

| ID | Requisito |
|---|---|
| RF01 | Permitir registrar lançamentos financeiros. |
| RF02 | Identificar lançamentos como crédito ou débito. |
| RF03 | Publicar lançamentos registrados para integração. |
| RF04 | Consumir lançamentos por meio de mensageria. |
| RF05 | Persistir os lançamentos recebidos. |
| RF06 | Consultar lançamentos recebidos. |
| RF07 | Consultar o consolidado financeiro por data. |
| RF08 | Calcular total de créditos, débitos e saldo diário. |
| RF09 | Evitar processamento duplicado de lançamentos. |

## 4. Requisitos não funcionais

- **Disponibilidade:** o serviço de lançamentos não deve ficar indisponível caso o serviço de consolidação esteja fora do ar.
- **Desempenho:** o serviço de consolidação deve suportar 50 requisições por segundo em dias de pico.
- **Confiabilidade:** a perda máxima de requisições no cenário de pico deve ser de 5%.
- **Resiliência:** utilizar mensageria, retentativas e Dead Letter Queue (DLQ).
- **Integridade:** garantir idempotência no processamento dos lançamentos.
- **Manutenibilidade:** manter responsabilidades separadas entre os serviços.

## 5. Arquitetura da solução

Foi adotada uma arquitetura baseada em microsserviços e orientada a eventos (Event-Driven Architecture).

### Componentes

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Maven

### Responsabilidades

**lancamentos-service**
- Disponibiliza APIs REST para controle dos lançamentos.
- Persiste os dados de origem.
- Publica mensagens no RabbitMQ.

**consolidacao-service**
- Consome mensagens da fila RabbitMQ.
- Valida e processa os lançamentos.
- Persiste os dados recebidos.
- Disponibiliza API REST para consulta do consolidado diário.

## 6. Fluxo de integração

1. O cliente solicita o registro de um lançamento.
2. O lancamentos-service valida e persiste o lançamento.
3. O serviço publica uma mensagem no RabbitMQ.
4. O consolidacao-service consome a mensagem.
5. O lançamento é validado e persistido no banco de consolidação.
6. O usuário consulta o consolidado diário por meio de uma API REST.

A comunicação assíncrona reduz o acoplamento entre os serviços e permite que o controle de lançamentos continue operando mesmo quando a consolidação estiver temporariamente indisponível, desde que o broker permaneça disponível.

## 7. Estratégias de resiliência

### RabbitMQ
Responsável pela comunicação assíncrona entre os serviços.

### Retentativas
Falhas temporárias no processamento das mensagens podem gerar novas tentativas, conforme política configurada.

### Dead Letter Queue (DLQ)
Mensagens que não puderem ser processadas são encaminhadas para uma fila específica para análise e tratamento posterior.

### Idempotência
O identificador do lançamento é utilizado como chave primária no serviço de consolidação, evitando a persistência duplicada do mesmo lançamento.

## 8. Segurança

Para uma implantação em produção, são previstas as seguintes medidas:

- Autenticação e autorização das APIs.
- Comunicação protegida por HTTPS/TLS.
- Credenciais armazenadas em variáveis de ambiente ou cofre de segredos.
- Controle de acesso ao RabbitMQ.
- Restrição de acesso aos bancos de dados.
- Validação dos dados recebidos pelas APIs e mensagens.

## 9. Observabilidade

Para evolução do ambiente, recomenda-se:

- Spring Boot Actuator para health checks.
- Métricas de processamento e consumo de mensagens.
- Monitoramento do tamanho das filas e da DLQ.
- Logs estruturados e correlacionados por identificador de lançamento.
- Integração futura com Prometheus e Grafana.

## 10. Escalabilidade e evolução

A arquitetura permite evoluções como:

- Escalar horizontalmente as instâncias do serviço de consolidação.
- Utilizar múltiplos consumidores RabbitMQ.
- Implementar cache para consultas frequentes.
- Criar relatórios e consultas por período.
- Implementar autenticação centralizada.
- Adotar padrão Outbox para garantir maior confiabilidade entre persistência e publicação de eventos.

## 11. Decisões arquiteturais

| Decisão | Justificativa |
|---|---|
| Microsserviços | Separação das responsabilidades de lançamento e consolidação. |
| RabbitMQ | Comunicação assíncrona e desacoplamento entre os serviços. |
| PostgreSQL | Persistência relacional e consistência dos dados financeiros. |
| Spring Boot | Produtividade, integração com mensageria e APIs REST. |
| Event-Driven Architecture | Permite processar lançamentos sem dependência síncrona da consolidação. |
| DLQ e retentativas | Tratamento de falhas de processamento. |

## 12. Limitações e melhorias futuras

A implementação apresentada representa uma versão inicial funcional da solução.

Para uma implantação produtiva, recomenda-se evoluir os seguintes pontos:

- Implementar padrão Outbox no serviço de lançamentos.
- Implementar autenticação e autorização.
- Configurar alta disponibilidade do RabbitMQ e PostgreSQL.
- Realizar testes de carga para validar 50 requisições por segundo.
- Implementar monitoramento e alertas.
- Avaliar particionamento e políticas de retenção de mensagens.
- Implementar estratégia de reprocessamento da DLQ.
