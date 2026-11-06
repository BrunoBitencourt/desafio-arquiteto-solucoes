
package br.com.desafio.consolidacao_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamentos_recebidos")
public class LancamentoRecebido {

    @Id
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    public LancamentoRecebido() {
    }

    public LancamentoRecebido(
            Long id,
            BigDecimal valor,
            String tipo,
            LocalDate dataLancamento) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.dataLancamento = dataLancamento;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }
}
