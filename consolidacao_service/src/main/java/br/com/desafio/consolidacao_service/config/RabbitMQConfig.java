
package br.com.desafio.consolidacao_service.config;

import br.com.desafio.consolidacao_service.consumer.LancamentoConsumer.MensagemInvalidaException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_DLQ = "lancamentos.criados.dlq";
    public static final String EXCHANGE_DLQ = "lancamentos.dlx";

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLQ, true, false);
    }

    @Bean
    public Queue filaDeadLetter() {
        return QueueBuilder.durable(FILA_DLQ).build();
    }

    @Bean
    public Binding bindingDeadLetter() {
        return BindingBuilder.bind(filaDeadLetter())
                .to(deadLetterExchange())
                .with(FILA_DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofSeconds(1))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(5))
                .includes(Exception.class)
                .excludes(MensagemInvalidaException.class)
                .build();

        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .retryPolicy(retryPolicy)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );

        return factory;
    }
}
