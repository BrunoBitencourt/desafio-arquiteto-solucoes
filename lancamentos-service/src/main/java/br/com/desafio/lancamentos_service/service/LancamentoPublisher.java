
package br.com.desafio.lancamentos_service.service;

import br.com.desafio.lancamentos_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class LancamentoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public LancamentoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(String mensagem) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FILA_LANCAMENTOS,
            mensagem
        );
    }
}
