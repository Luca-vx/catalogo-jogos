package com.catalogo.gateway.aggregator;

import com.catalogo.gateway.adapter.OfertaLojaPort;
import com.catalogo.gateway.client.AvaliacoesClient;
import com.catalogo.gateway.client.CatalogoClient;
import com.catalogo.gateway.dto.AvaliacaoResumoDTO;
import com.catalogo.gateway.dto.GameCompletoDTO;
import com.catalogo.gateway.dto.GameDTO;
import com.catalogo.gateway.dto.OfertaLojaDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Padrão Aggregator.
 *
 * Responsável por reunir informações de múltiplos microsserviços:
 * 1. Serviço A (Catálogo - Porta 8081): Dados cadastrais do jogo.
 * 2. Serviço B (Avaliações - Porta 8082): Resumo das avaliações e comentários.
 * 3. Serviço C (Loja Externa - Porta 8083, via Adapter): Preço e estoque convertidos.
 *
 * Implementa resiliência e degradação graciosa: falhas em serviços secundários
 * (avaliações ou preços) não impedem a entrega dos dados principais do jogo.
 */
@Service
public class GameAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(GameAggregatorService.class);

    private final OfertaLojaPort ofertaLojaPort;
    private final AvaliacoesClient avaliacoesClient;
    private final CatalogoClient catalogoClient;

    public GameAggregatorService(OfertaLojaPort ofertaLojaPort,
                                 AvaliacoesClient avaliacoesClient,
                                 CatalogoClient catalogoClient) {
        this.ofertaLojaPort = ofertaLojaPort;
        this.avaliacoesClient = avaliacoesClient;
        this.catalogoClient = catalogoClient;
    }

    public Optional<GameCompletoDTO> buscarGameCompleto(Long gameId) {
        log.info("[AGGREGATOR] Iniciando agregação de informações para o jogo id: {}", gameId);

        // 1. Consulta ao Serviço A (Catálogo - essencial)
        GameDTO game = catalogoClient.buscarPorId(gameId);
        if (game == null) {
            log.warn("[AGGREGATOR] Jogo {} não encontrado no Serviço de Catálogo.", gameId);
            return Optional.empty();
        }

        List<String> avisosIntegracao = new ArrayList<>();

        // 2. Consulta ao Serviço B (Avaliações - secundário com fallback)
        AvaliacaoResumoDTO avaliacoes = avaliacoesClient.buscarResumo(gameId, avisosIntegracao);

        // 3. Consulta ao Serviço C via Adapter (Loja Externa - secundário com fallback)
        OfertaLojaDTO melhorOferta = consultarLojaExternaViaAdapter(gameId, avisosIntegracao);

        String statusIntegracao = avisosIntegracao.isEmpty()
                ? "INTEGRAÇÃO_COMPLETA_COM_SUCESSO"
                : "INTEGRAÇÃO_PARCIAL: " + String.join("; ", avisosIntegracao);

        GameCompletoDTO resultado = new GameCompletoDTO(
                game.id(),
                game.titulo(),
                game.genero(),
                game.plataforma(),
                game.desenvolvedora(),
                game.nota(),
                avaliacoes,
                melhorOferta,
                statusIntegracao
        );

        log.info("[AGGREGATOR] Agregação concluída com sucesso para o jogo: {}", game.titulo());
        return Optional.of(resultado);
    }

    private OfertaLojaDTO consultarLojaExternaViaAdapter(Long gameId, List<String> avisos) {
        try {
            Optional<OfertaLojaDTO> ofertaOpt = ofertaLojaPort.buscarOfertaPorGameId(gameId);
            if (ofertaOpt.isPresent()) {
                return ofertaOpt.get();
            } else {
                avisos.add("Nenhuma oferta encontrada na loja parceira para este jogo");
                return null;
            }
        } catch (Exception ex) {
            log.warn("[AGGREGATOR] Falha na integração com Loja Externa via Adapter: {}. Degradação graciosa aplicada.", ex.getMessage());
            avisos.add("Serviço de loja externa indisponível");
            return null;
        }
    }
}

