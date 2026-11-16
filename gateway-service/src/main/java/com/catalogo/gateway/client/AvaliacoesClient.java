package com.catalogo.gateway.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.catalogo.gateway.dto.AvaliacaoResumoDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class AvaliacoesClient {

    private static final Logger log = LoggerFactory.getLogger(AvaliacoesClient.class);

    private final RestClient restClient;
    private final String avaliacoesUrl;

    public AvaliacoesClient(RestClient restClient,
                            @Value("${servicos.avaliacoes.url}") String avaliacoesUrl) {
        this.restClient = restClient;
        this.avaliacoesUrl = avaliacoesUrl;
    }

    @Retry(name = "avaliacoesRetry", fallbackMethod = "fallbackResumo")
    @CircuitBreaker(name = "avaliacoesBreaker")
    public AvaliacaoResumoDTO buscarResumo(Long gameId, List<String> avisos) {
        String url = avaliacoesUrl + "/avaliacoes/game/" + gameId + "/resumo";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(AvaliacaoResumoDTO.class);
    }

    public AvaliacaoResumoDTO fallbackResumo(Long gameId, List<String> avisos, Throwable t) {
        log.warn("[AGGREGATOR] Não foi possível consultar o Serviço B de Avaliações: {}. Aplicando degradação graciosa.", t.getMessage());
        avisos.add("Serviço de avaliações temporariamente indisponível ou sem dados");
        return new AvaliacaoResumoDTO(gameId, 0.0, 0, List.of());
    }
}
