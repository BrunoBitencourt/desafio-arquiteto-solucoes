
package br.com.desafio.lancamentos_service.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lancamentos")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoLancamento tipo;

    @Column(nullable = false)
    private LocalDate dataLancamento;

    @Column(nullable = false, length = 250)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Lancamento() {
    }

    public Lancamento(
            BigDecimal valor,
            TipoLancamento tipo,
            LocalDate dataLancamento,
            String descricao) {
        this.valor = valor;
        this.tipo = tipo;
        this.dataLancamento = dataLancamento;
        this.descricao = descricao;
        this.criadoEm = LocalDateTime.now();
    }

    @PrePersist
    protected void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
