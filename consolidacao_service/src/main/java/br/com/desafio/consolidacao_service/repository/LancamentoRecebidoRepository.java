

package br.com.desafio.consolidacao_service.repository;

import br.com.desafio.consolidacao_service.entity.LancamentoRecebido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LancamentoRecebidoRepository
        extends JpaRepository<LancamentoRecebido, Long> {

    List<LancamentoRecebido> findByDataLancamento(LocalDate dataLancamento);
}

