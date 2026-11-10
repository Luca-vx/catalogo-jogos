package com.catalogo.avaliacoes.controller;

import com.catalogo.avaliacoes.dto.AvaliacaoResumoResponse;
import com.catalogo.avaliacoes.entity.Avaliacao;
import com.catalogo.avaliacoes.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avaliacao adicionarAvaliacao(@RequestBody Avaliacao avaliacao) {
        return service.adicionarAvaliacao(avaliacao);
    }

    @GetMapping("/game/{gameId}")
    public List<Avaliacao> buscarPorJogo(@PathVariable Long gameId) {
        return service.buscarPorJogo(gameId);
    }

    @GetMapping("/game/{gameId}/resumo")
    public AvaliacaoResumoResponse obterResumoPorJogo(@PathVariable Long gameId) {
        return service.obterResumoPorJogo(gameId);
    }
}

