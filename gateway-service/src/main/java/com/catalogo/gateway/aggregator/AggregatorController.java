package com.catalogo.gateway.aggregator;

import com.catalogo.gateway.dto.GameCompletoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador do Aggregator.
 *
 * Fornece endpoints agregados para entregar uma visão completa e enriquecida
 * de um jogo, combinando dados de catálogo (Serviço A), avaliações (Serviço B)
 * e ofertas da loja parceira através do Adapter (Serviço C).
 */
@RestController
public class AggregatorController {

    @Autowired
    private GameAggregatorService aggregatorService;

    @GetMapping({"/game/{id}/completo", "/game/{id}/detalhes"})
    public ResponseEntity<GameCompletoDTO> buscarGameCompleto(@PathVariable Long id) {
        return aggregatorService.buscarGameCompleto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

