
package br.com.desafio.consolidacao_service.service;

import br.com.desafio.consolidacao_service.entity.ConsolidadoDiario;
import br.com.desafio.consolidacao_service.entity.LancamentoRecebido;
import br.com.desafio.consolidacao_service.repository.LancamentoRecebidoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LancamentoRecebidoServiceTest {

    @Mock
    private LancamentoRecebidoRepository repository;

    @InjectMocks
    private LancamentoRecebidoService service;

    @Test
    void deveCalcularConsolidadoDiarioCorretamente() {

        LocalDate data = LocalDate.of(2026, 10, 22);

        List<LancamentoRecebido> lancamentos = List.of(
                new LancamentoRecebido(
                        101L, new BigDecimal("73.00"), "DEBITO", data),
                new LancamentoRecebido(
                        102L, new BigDecimal("150.00"), "CREDITO", data)
        );

        when(repository.findByDataLancamento(data))
                .thenReturn(lancamentos);

        ConsolidadoDiario resultado =
                service.consultarConsolidado(data);

        assertEquals(new BigDecimal("150.00"), resultado.getTotalCreditos());
        assertEquals(new BigDecimal("73.00"), resultado.getTotalDebitos());
        assertEquals(new BigDecimal("77.00"), resultado.getSaldo());
    }

    @Test
    void deveRetornarSaldoNegativoQuandoDebitosForemMaiores() {

        LocalDate data = LocalDate.of(2026, 10, 23);

        List<LancamentoRecebido> lancamentos = List.of(
                new LancamentoRecebido(
                        103L, new BigDecimal("50.00"), "CREDITO", data),
                new LancamentoRecebido(
                        104L, new BigDecimal("80.00"), "DEBITO", data)
        );

        when(repository.findByDataLancamento(data))
                .thenReturn(lancamentos);

        ConsolidadoDiario resultado = service.consultarConsolidado(data);

        assertEquals(new BigDecimal("-30.00"), resultado.getSaldo());
    }

    @Test
    void deveRetornarValoresZeradosQuandoNaoHouverLancamentos() {

        LocalDate data = LocalDate.of(2026, 10, 24);

        when(repository.findByDataLancamento(data))
                .thenReturn(List.of());

        ConsolidadoDiario resultado = service.consultarConsolidado(data);

        assertEquals(BigDecimal.ZERO, resultado.getTotalCreditos());
        assertEquals(BigDecimal.ZERO, resultado.getTotalDebitos());
        assertEquals(BigDecimal.ZERO, resultado.getSaldo());
    }
}
