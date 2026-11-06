
package br.com.desafio.consolidacao_service.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ConsolidadoDiario {

    private LocalDate data;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldo;

    public ConsolidadoDiario(
            LocalDate data,
            BigDecimal totalCreditos,
            BigDecimal totalDebitos,
            BigDecimal saldo) {

        this.data = data;
        this.totalCreditos = totalCreditos;
        this.totalDebitos = totalDebitos;
        this.saldo = saldo;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getTotalCreditos() {
        return totalCreditos;
    }

    public BigDecimal getTotalDebitos() {
        return totalDebitos;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
