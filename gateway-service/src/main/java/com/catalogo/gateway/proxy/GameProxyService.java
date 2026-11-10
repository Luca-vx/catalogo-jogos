package com.catalogo.gateway.proxy;

import com.catalogo.gateway.dto.GameDTO;
import com.catalogo.gateway.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Padrão Proxy.
 *
 * Atua como intermediário transparente entre os clientes da API e o Serviço A (Catálogo).
 * Encaminha as operações CRUD, oculta os detalhes de rede internos (porta 8081)
 * e implementa tratamento de indisponibilidade e falhas de comunicação.
 */
@Service
public class GameProxyService {

    private static final Logger log = LoggerFactory.getLogger(GameProxyService.class);

    private final RestClient restClient;
    private final String catalogoUrl;

    public GameProxyService(RestClient restClient,
                            @Value("${servicos.catalogo.url}") String catalogoUrl) {
        this.restClient = restClient;
        this.catalogoUrl = catalogoUrl;
    }

    public ResponseEntity<GameDTO> adicionarGame(GameDTO game) {
        String url = catalogoUrl + "/game";
        log.info("[PROXY] Encaminhando POST para: {}", url);
        try {
            return restClient.post()
                    .uri(url)
                    .body(game)
                    .retrieve()
                    .toEntity(GameDTO.class);
        } catch (ResourceAccessException ex) {
            throw handleUnavailable(ex);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(null);
        }
    }

    public ResponseEntity<List<GameDTO>> buscarTodosGames() {
        String url = catalogoUrl + "/game";
        log.info("[PROXY] Encaminhando GET para: {}", url);
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<GameDTO>>() {});
        } catch (ResourceAccessException ex) {
            throw handleUnavailable(ex);
        }
    }

    public ResponseEntity<GameDTO> buscarPorId(Long id) {
        String url = catalogoUrl + "/game/" + id;
        log.info("[PROXY] Encaminhando GET por ID para: {}", url);
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(GameDTO.class);
        } catch (ResourceAccessException ex) {
            throw handleUnavailable(ex);
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(null);
        }
    }

    public ResponseEntity<GameDTO> atualizarGame(Long id, GameDTO game) {
        String url = catalogoUrl + "/game/" + id;
        log.info("[PROXY] Encaminhando PUT para: {}", url);
        try {
            return restClient.put()
                    .uri(url)
                    .body(game)
                    .retrieve()
                    .toEntity(GameDTO.class);
        } catch (ResourceAccessException ex) {
            throw handleUnavailable(ex);
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(null);
        }
    }

    public ResponseEntity<Void> deletarGame(Long id) {
        String url = catalogoUrl + "/game/" + id;
        log.info("[PROXY] Encaminhando DELETE para: {}", url);
        try {
            return restClient.delete()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException ex) {
            throw handleUnavailable(ex);
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    private ServiceUnavailableException handleUnavailable(Exception ex) {
        log.error("[PROXY] Serviço A (Catálogo) na porta 8081 está indisponível: {}", ex.getMessage());
        return new ServiceUnavailableException("Serviço de Catálogo temporariamente indisponível. Tente novamente mais tarde.");
    }
}

