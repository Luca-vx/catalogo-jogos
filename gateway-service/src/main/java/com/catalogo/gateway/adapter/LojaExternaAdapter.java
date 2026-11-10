package com.catalogo.gateway.adapter;

import com.catalogo.gateway.adapter.dto.LegacyStorePricingResponse;
import com.catalogo.gateway.dto.OfertaLojaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Padrão Adapter (Object Adapter).
 *
 * Responsável por adaptar a interface e a estrutura de dados heterogênea do
 * Serviço C (API legada de precificação em centavos e snake_case)
 * para a interface de domínio esperada pela nossa aplicação (OfertaLojaPort e OfertaLojaDTO).
 */
@Component
public class LojaExternaAdapter implements OfertaLojaPort {

    private static final Logger log = LoggerFactory.getLogger(LojaExternaAdapter.class);

    private final RestClient restClient;
    private final String servicoLojaUrl;

    public LojaExternaAdapter(RestClient restClient,
                              @Value("${servicos.loja.url}") String servicoLojaUrl) {
        this.restClient = restClient;
        this.servicoLojaUrl = servicoLojaUrl;
    }

    @Override
    public Optional<OfertaLojaDTO> buscarOfertaPorGameId(Long gameId) {
        String url = servicoLojaUrl + "/api/v1/legacy-store/pricing/" + gameId;
        log.info("[ADAPTER] Consultando sistema legado de loja em: {}", url);

        try {
            LegacyStorePricingResponse legacyResponse = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        log.warn("[ADAPTER] Jogo {} não possui ofertas cadastradas na loja externa (HTTP {}).",
                                gameId, resp.getStatusCode());
                    })
                    .body(LegacyStorePricingResponse.class);

            if (legacyResponse == null) {
                return Optional.empty();
            }

            // Conversão / Adaptação das estruturas de dados:
            // 1. Centavos (Integer) -> Valor Monetário Real (Double)
            Double precoConvertido = (legacyResponse.priceAmountCents() != null)
                    ? legacyResponse.priceAmountCents() / 100.0
                    : 0.0;

            // 2. Status textual ("AVAILABLE") -> Booleano de disponibilidade
            boolean disponivel = "AVAILABLE".equalsIgnoreCase(legacyResponse.inventoryStatus());

            OfertaLojaDTO ofertaAdaptada = new OfertaLojaDTO(
                    legacyResponse.vendorName() != null ? legacyResponse.vendorName() : "Loja Desconhecida",
                    precoConvertido,
                    legacyResponse.isoCurrency() != null ? legacyResponse.isoCurrency() : "BRL",
                    disponivel,
                    legacyResponse.deeplink() != null ? legacyResponse.deeplink() : ""
            );

            log.info("[ADAPTER] Dados adaptados com sucesso para o jogo {}: Loja={}, Preço=R${}",
                    gameId, ofertaAdaptada.loja(), ofertaAdaptada.preco());

            return Optional.of(ofertaAdaptada);

        } catch (Exception ex) {
            log.error("[ADAPTER] Falha ao comunicar com o serviço de loja externa: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}

