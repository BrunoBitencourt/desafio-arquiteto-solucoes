
package br.com.desafio.consolidacao_service.controller;

import br.com.desafio.consolidacao_service.entity.ConsolidadoDiario;
import br.com.desafio.consolidacao_service.entity.LancamentoRecebido;
import br.com.desafio.consolidacao_service.repository.LancamentoRecebidoRepository;
import br.com.desafio.consolidacao_service.service.LancamentoRecebidoService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lancamentos-recebidos")
public class LancamentoRecebidoController {

    private final LancamentoRecebidoRepository repository;
    private final LancamentoRecebidoService service;

    public LancamentoRecebidoController(
            LancamentoRecebidoRepository repository,
            LancamentoRecebidoService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public List<LancamentoRecebido> listar() {
        return repository.findAll();
    }

    @GetMapping("/consolidado")
    public ConsolidadoDiario consultarConsolidado(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {

        return service.consultarConsolidado(data);
    }
}
