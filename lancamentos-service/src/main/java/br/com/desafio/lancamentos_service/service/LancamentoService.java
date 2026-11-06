
package br.com.desafio.lancamentos_service.service;

import br.com.desafio.lancamentos_service.domain.Lancamento;
import br.com.desafio.lancamentos_service.repository.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LancamentoService {

    private final LancamentoRepository repository;
    private final LancamentoPublisher publisher;

    public LancamentoService(
            LancamentoRepository repository,
            LancamentoPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public Lancamento registrar(Lancamento lancamento) {
        Lancamento salvo = repository.save(lancamento);

        publisher.publicar(
            "Lançamento ID: " + salvo.getId()
                + ", valor: " + salvo.getValor()
                + ", tipo: " + salvo.getTipo()
                + ", data: " + salvo.getDataLancamento()
        );

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Lancamento> listarTodos() {
        return repository.findAll();
    }
}
