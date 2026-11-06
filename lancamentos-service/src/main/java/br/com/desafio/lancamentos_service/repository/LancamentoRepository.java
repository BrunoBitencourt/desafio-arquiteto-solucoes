
package br.com.desafio.lancamentos_service.repository;

import br.com.desafio.lancamentos_service.domain.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository
        extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByDataLancamento(LocalDate dataLancamento);
}
