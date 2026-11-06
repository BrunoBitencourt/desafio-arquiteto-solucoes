
package br.com.desafio.consolidacao_service.consumer;

import br.com.desafio.consolidacao_service.entity.LancamentoRecebido;
import br.com.desafio.consolidacao_service.service.LancamentoRecebidoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;

@Component
public class LancamentoConsumer {

    private final LancamentoRecebidoService service;

    public LancamentoConsumer(LancamentoRecebidoService service) {
        this.service = service;
    }

    @RabbitListener(
            queues = "lancamentos.criados",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receber(String mensagem) {
        System.out.println(
                "Lançamento recebido do RabbitMQ: " + mensagem);

        String[] campos = mensagem.split(", ");

        if (campos.length != 4
                || !campos[0].startsWith("Lançamento ID: ")
                || !campos[1].startsWith("valor: ")
                || !campos[2].startsWith("tipo: ")
                || !campos[3].startsWith("data: ")) {
            throw new MensagemInvalidaException(
                    "Formato de mensagem inválido");
        }

        try {
            Long id = Long.parseLong(
                    campos[0].substring("Lançamento ID: ".length()).trim());

            BigDecimal valor = new BigDecimal(
                    campos[1].substring("valor: ".length()).trim());

            String tipo = campos[2]
                    .substring("tipo: ".length()).trim();

            LocalDate data = LocalDate.parse(
                    campos[3].substring("data: ".length()).trim());

            if (id <= 0) {
                throw new MensagemInvalidaException(
                        "O ID deve ser positivo");
            }

            if (valor.signum() <= 0) {
                throw new MensagemInvalidaException(
                        "O valor deve ser maior que zero");
            }

            if (!tipo.equals("CREDITO") && !tipo.equals("DEBITO")) {
                throw new MensagemInvalidaException(
                        "Tipo deve ser CREDITO ou DEBITO");
            }

            LancamentoRecebido lancamento =
                    new LancamentoRecebido(id, valor, tipo, data);

            service.salvar(lancamento);

        } catch (NumberFormatException | DateTimeException e) {
            throw new MensagemInvalidaException(
                    "Valor, ID ou data inválidos", e);
        }
    }

    public static class MensagemInvalidaException
            extends IllegalArgumentException {

        public MensagemInvalidaException(String mensagem) {
            super(mensagem);
        }

        public MensagemInvalidaException(
                String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }
}
