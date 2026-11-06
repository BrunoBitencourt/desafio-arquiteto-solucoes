
package br.com.desafio.lancamentos_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_LANCAMENTOS = "lancamentos.criados";

    @Bean
    public Queue filaLancamentos() {
        return new Queue(FILA_LANCAMENTOS, true);
    }
}
