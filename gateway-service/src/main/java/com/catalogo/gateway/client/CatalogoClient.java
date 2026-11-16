package com.catalogo.gateway.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.catalogo.gateway.dto.GameDTO;
import com.catalogo.gateway.exception.ServiceUnavailableException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class CatalogoClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClient.class);

    private final RestClient restClient;
    private final String catalogoUrl;

    public CatalogoClient(RestClient restClient,
                          @Value("${servicos.catalogo.url}") String catalogoUrl) {
        this.restClient = restClient;
        this.catalogoUrl = catalogoUrl;
    }

    @Retry(name = "catalogoRetry", fallbackMethod = "fallbackCatalogo")
    @CircuitBreaker(name = "catalogoBreaker")
    public GameDTO buscarPorId(Long gameId) {
        String url = catalogoUrl + "/game/" + gameId;
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(GameDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }

    public GameDTO fallbackCatalogo(Long gameId, Throwable t) {
        log.error("[CATALOGO] Fallback acionado para jogo {}: {}", gameId, t.getMessage());
        throw new ServiceUnavailableException("Serviço de Catálogo temporariamente indisponível.");
    }
}
