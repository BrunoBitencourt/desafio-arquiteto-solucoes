
package br.com.desafio.lancamentos_service.controller;

import br.com.desafio.lancamentos_service.domain.Lancamento;
import br.com.desafio.lancamentos_service.service.LancamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoController {

    private final LancamentoService service;

    public LancamentoController(LancamentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Lancamento registrar(@RequestBody Lancamento lancamento) {
        return service.registrar(lancamento);
    }

    @GetMapping
    public List<Lancamento> listarTodos() {
        return service.listarTodos();
    }
}
