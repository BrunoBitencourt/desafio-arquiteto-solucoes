
package br.com.desafio.consolidacao_service.service;

import br.com.desafio.consolidacao_service.entity.ConsolidadoDiario;
import br.com.desafio.consolidacao_service.entity.LancamentoRecebido;
import br.com.desafio.consolidacao_service.repository.LancamentoRecebidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LancamentoRecebidoService {

    private final LancamentoRecebidoRepository repository;

    public LancamentoRecebidoService(
            LancamentoRecebidoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LancamentoRecebido salvar(LancamentoRecebido lancamento) {

        if (repository.existsById(lancamento.getId())) {
            System.out.println(
                    "Lançamento duplicado ignorado: " + lancamento.getId());

            return repository.findById(lancamento.getId()).orElseThrow();
        }

        return repository.save(lancamento);
    }

    @Transactional(readOnly = true)
    public ConsolidadoDiario consultarConsolidado(LocalDate data) {

        List<LancamentoRecebido> lancamentos =
                repository.findByDataLancamento(data);

        BigDecimal totalCreditos = BigDecimal.ZERO;
        BigDecimal totalDebitos = BigDecimal.ZERO;

        for (LancamentoRecebido lancamento : lancamentos) {

            if ("CREDITO".equals(lancamento.getTipo())) {
                totalCreditos = totalCreditos.add(lancamento.getValor());

            } else if ("DEBITO".equals(lancamento.getTipo())) {
                totalDebitos = totalDebitos.add(lancamento.getValor());
            }
        }

        BigDecimal saldo = totalCreditos.subtract(totalDebitos);

        return new ConsolidadoDiario(
                data,
                totalCreditos,
                totalDebitos,
                saldo
        );
    }
}
