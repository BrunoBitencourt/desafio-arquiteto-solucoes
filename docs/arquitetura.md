# Arquitetura da Solução — Controle de Fluxo de Caixa

## 1. Objetivo

Desenvolver uma solução para permitir que comerciantes controlem seus lançamentos financeiros diários, registrando créditos e débitos e consultando o saldo consolidado por data.

A solução foi dividida em dois serviços independentes:

- **lancamentos-service:** responsável pelo registro e controle dos lançamentos.
- **consolidacao-service:** responsável pelo recebimento dos lançamentos e cálculo do consolidado diário.

O objetivo da separação é permitir que o registro de lançamentos continue operando independentemente do processamento da consolidação, reduzindo o acoplamento entre as responsabilidades.

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

- **Disponibilidade:** o serviço de lançamentos não deve ficar indisponível caso o serviço de consolidação esteja fora do ar, desde que suas dependências essenciais estejam disponíveis.
- **Desempenho:** o serviço de consolidação deve suportar 50 requisições por segundo em dias de pico.
- **Confiabilidade:** a perda máxima de requisições no cenário de pico deve ser de 5%.
- **Resiliência:** utilizar mensageria, retentativas e Dead Letter Queue (DLQ).
- **Integridade:** garantir idempotência no processamento dos lançamentos.
- **Manutenibilidade:** manter responsabilidades separadas entre os serviços.
- **Escalabilidade:** permitir aumentar a capacidade de processamento da consolidação por meio de múltiplas instâncias consumidoras.

### 4.1. Estratégia de validação de desempenho e confiabilidade

Para validar os requisitos de desempenho e confiabilidade, recomenda-se realizar testes de carga simulando o cenário de pico.

Os testes deverão considerar:

- Envio sustentado de 50 requisições por segundo.
- Medição da quantidade de mensagens recebidas e processadas.
- Monitoramento do tamanho da fila RabbitMQ.
- Identificação de mensagens rejeitadas, não processadas ou encaminhadas para DLQ.
- Medição do tempo de processamento e da latência das APIs.
- Simulação de indisponibilidade temporária do serviço de consolidação.
- Verificação do processamento posterior das mensagens acumuladas.

O critério de aceitação será manter a perda de requisições em até 5% durante o cenário de carga definido.

**Observação:** os testes de carga ainda precisam ser executados para comprovar o atendimento desse requisito. A arquitetura foi projetada considerando esse objetivo, mas não se afirma que a capacidade já foi comprovada.

## 5. Arquitetura da solução

Foi adotada uma arquitetura baseada em microsserviços e orientada a eventos (Event-Driven Architecture).

A comunicação entre os serviços ocorre de forma assíncrona, utilizando RabbitMQ.

### Componentes tecnológicos

- Java 21
- Spring Boot 4.0.8
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Maven
- Flyway

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

### 5.1. Separação de dados

Cada microsserviço possui seu próprio banco de dados PostgreSQL:

- `lancamentos_db`: utilizado pelo serviço de lançamentos.
- `consolidacao_db`: utilizado pelo serviço de consolidação.

Essa separação evita o compartilhamento direto de tabelas entre os serviços e favorece a autonomia de cada domínio.

## 6. Fluxo de integração

1. O cliente solicita o registro de um lançamento.
2. O `lancamentos-service` valida e persiste o lançamento.
3. O serviço publica uma mensagem no RabbitMQ.
4. O `consolidacao-service` consome a mensagem.
5. O lançamento é validado e persistido no banco de consolidação.
6. O usuário consulta o consolidado diário por meio de uma API REST.

A comunicação assíncrona reduz o acoplamento entre os serviços e permite que o controle de lançamentos continue operando mesmo quando a consolidação estiver temporariamente indisponível, desde que o banco de lançamentos e a infraestrutura de mensageria permaneçam disponíveis.

### 6.1. Consistência dos dados

A arquitetura utiliza consistência eventual entre os serviços.

Isso significa que um lançamento pode ser registrado no serviço de origem antes de estar disponível no consolidado diário.

O tempo de atualização depende do processamento das mensagens pelo serviço de consolidação.

A separação dos bancos de dados garante autonomia, mas exige mecanismos de integração e recuperação para manter a consistência entre os domínios.

## 7. Estratégias de resiliência

### 7.1. RabbitMQ

Responsável pela comunicação assíncrona entre os serviços, permitindo desacoplar o registro dos lançamentos do processamento da consolidação.

A fila principal utilizada é `lancamentos.criados`.

### 7.2. Retentativas

Falhas temporárias no processamento das mensagens podem gerar novas tentativas, conforme política configurada.

Na implementação atual, foram configuradas até duas retentativas, com intervalo inicial de um segundo, multiplicador de dois e intervalo máximo de cinco segundos.

### 7.3. Dead Letter Queue (DLQ)

Mensagens que não puderem ser processadas são encaminhadas para uma fila específica para análise e tratamento posterior.

A fila utilizada é `lancamentos.criados.dlq`.

A DLQ permite isolar mensagens com falhas persistentes sem interromper o processamento das demais mensagens.

### 7.4. Idempotência

O identificador do lançamento é utilizado como chave primária no serviço de consolidação, evitando a persistência duplicada do mesmo lançamento.

Essa estratégia permite lidar com a entrega repetida de mensagens sem duplicar os registros.

### 7.5. Risco de inconsistência entre banco e mensageria

Na implementação atual, o lançamento é persistido no PostgreSQL e, posteriormente, publicado no RabbitMQ.

Existe um possível cenário de falha: o lançamento pode ser salvo no banco de dados, mas a publicação da mensagem pode falhar.

Nesse caso, o registro existe no serviço de origem, mas não chega ao serviço de consolidação.

A DLQ e as retentativas do consumidor não resolvem esse cenário, pois atuam sobre mensagens que já chegaram ao fluxo de consumo.

### 7.6. Evolução com Transactional Outbox

Como evolução arquitetural, recomenda-se implementar o padrão Transactional Outbox.

Nesse modelo:

1. O lançamento é persistido no banco de dados.
2. O evento correspondente é registrado em uma tabela Outbox.
3. Ambos são gravados na mesma transação.
4. Um processo independente identifica os eventos pendentes e os publica no RabbitMQ.
5. Após a publicação confirmada, o evento é marcado como processado.

Essa abordagem reduz o risco de inconsistência entre a persistência do lançamento e a publicação do evento.

A implementação do padrão Outbox não faz parte da versão atual da solução.

## 8. Segurança

A solução considera os seguintes critérios de segurança:

- Autenticação e autorização das APIs.
- Comunicação protegida por HTTPS/TLS.
- Credenciais armazenadas em variáveis de ambiente ou cofre de segredos.
- Controle de acesso ao RabbitMQ.
- Restrição de acesso aos bancos de dados.
- Validação dos dados recebidos pelas APIs e mensagens.
- Não exposição de informações sensíveis nos logs.

As credenciais de execução local são fornecidas por variáveis de ambiente, evitando manter senhas diretamente nos arquivos de configuração versionados.

A implementação de mecanismos avançados de autenticação, autorização e gerenciamento centralizado de segredos está prevista como evolução para um ambiente produtivo.

## 9. Observabilidade

A solução possui configuração do Spring Boot Actuator para disponibilização de endpoints de saúde e métricas.

Para evolução do ambiente, recomenda-se:

- Monitoramento dos endpoints de saúde dos microsserviços.
- Acompanhamento das métricas de processamento e consumo de mensagens.
- Monitoramento do tamanho das filas e da DLQ.
- Logs estruturados e correlacionados por identificador de lançamento.
- Monitoramento de latência e taxa de erros das APIs.
- Integração futura com Prometheus e Grafana.
- Configuração de alertas para indisponibilidade e crescimento excessivo das filas.

A integração com Prometheus, Grafana e uma solução centralizada de logs não está implementada nesta versão.

## 10. Escalabilidade e evolução

A arquitetura permite evoluções como:

- Escalar horizontalmente as instâncias do serviço de consolidação.
- Utilizar múltiplos consumidores RabbitMQ.
- Configurar concorrência de consumidores conforme a capacidade do banco de dados.
- Implementar cache para consultas frequentes.
- Criar relatórios e consultas por período.
- Implementar autenticação centralizada.
- Adotar o padrão Outbox para garantir maior confiabilidade entre persistência e publicação de eventos.
- Avaliar particionamento e políticas de retenção de mensagens.

A escalabilidade horizontal deverá considerar a capacidade do PostgreSQL, a concorrência de processamento e a configuração das filas RabbitMQ.

## 11. Decisões arquiteturais

| Decisão | Justificativa |
|---|---|
| Microsserviços | Separação das responsabilidades de lançamento e consolidação. |
| RabbitMQ | Comunicação assíncrona e desacoplamento entre os serviços. |
| PostgreSQL | Persistência relacional e consistência dos dados financeiros. |
| Bancos separados por serviço | Isolamento dos dados e autonomia dos domínios. |
| Spring Boot | Produtividade, integração com mensageria e APIs REST. |
| Event-Driven Architecture | Permite processar lançamentos sem dependência síncrona da consolidação. |
| DLQ e retentativas | Tratamento de falhas de processamento. |
| Idempotência | Prevenção de duplicidade no processamento das mensagens. |

### 11.1. Trade-offs arquiteturais

| Decisão | Benefício | Trade-off |
|---|---|---|
| Microsserviços | Independência de implantação e responsabilidades separadas. | Maior complexidade operacional e de comunicação. |
| RabbitMQ | Comunicação assíncrona e desacoplamento. | Necessidade de monitoramento e gestão das filas. |
| PostgreSQL separado por serviço | Isolamento dos dados e autonomia. | Maior custo de infraestrutura e complexidade de consistência. |
| Consistência eventual | Permite processamento assíncrono. | O consolidado pode apresentar atraso em relação ao lançamento original. |
| DLQ | Isola mensagens com falhas persistentes. | Exige análise e estratégia de reprocessamento. |
| Transactional Outbox (evolução) | Reduz o risco de inconsistência entre banco e publicação. | Introduz uma tabela de eventos e um processo adicional de publicação. |

## 12. Limitações e melhorias futuras

A implementação apresentada representa uma versão inicial funcional da solução, com foco na separação de responsabilidades, integração assíncrona e consolidação financeira.

Para uma implantação produtiva, recomenda-se evoluir os seguintes pontos:

- Implementar o padrão Transactional Outbox no serviço de lançamentos.
- Implementar autenticação e autorização das APIs.
- Configurar alta disponibilidade do RabbitMQ e PostgreSQL.
- Realizar testes de carga para validar 50 requisições por segundo e perda máxima de 5%.
- Implementar monitoramento e alertas.
- Desenvolver estratégia de reprocessamento da DLQ.
- Avaliar mecanismos de recuperação de eventos não publicados.
- Definir políticas de retenção e recuperação de dados.
- Realizar testes de integração e resiliência simulando falhas de infraestrutura.

## 13. Status da implementação

Para diferenciar o que já foi desenvolvido do que representa uma evolução planejada, os itens são classificados da seguinte forma:

| Recurso | Situação |
|---|---|
| Registro de lançamentos | Implementado |
| Persistência no PostgreSQL | Implementado |
| Publicação e consumo via RabbitMQ | Implementado |
| Consolidação diária | Implementado |
| Idempotência no recebimento | Implementado |
| Retentativas de processamento | Configurado |
| Dead Letter Queue | Configurado |
| Variáveis de ambiente para credenciais | Configurado |
| Spring Boot Actuator | Configurado |
| Transactional Outbox | Planejado |
| Autenticação e autorização avançadas | Planejado |
| Prometheus e Grafana | Planejado |
| Testes de carga de 50 requisições por segundo | Pendente de validação |
| Alta disponibilidade de infraestrutura | Planejado |

## 14. Considerações finais

A solução demonstra uma arquitetura baseada em microsserviços e orientada a eventos, com separação de responsabilidades, persistência independente e comunicação assíncrona.

O uso de RabbitMQ, retentativas, DLQ e idempotência contribui para a confiabilidade do processamento.

As evoluções propostas, especialmente o padrão Transactional Outbox, a observabilidade avançada e os testes de carga, permitem aproximar a solução de um ambiente produtivo, considerando os requisitos de disponibilidade, desempenho e confiabilidade.