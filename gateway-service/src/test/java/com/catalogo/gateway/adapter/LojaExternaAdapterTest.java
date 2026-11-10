package com.catalogo.gateway.adapter;

import com.catalogo.gateway.dto.OfertaLojaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class LojaExternaAdapterTest {

    private MockRestServiceServer mockServer;
    private LojaExternaAdapter adapter;

    private final String baseUrl = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        this.adapter = new LojaExternaAdapter(builder.build(), baseUrl);
    }

    @Test
    @DisplayName("Deve converter com sucesso a resposta legada do Serviço C para o modelo OfertaLojaDTO")
    void deveConverterDadosLegadosComSucesso() {
        String jsonResposta = """
                {
                  "game_identifier": 1,
                  "external_sku": "SKU-TW3-001",
                  "vendor_name": "Steam Partner Store",
                  "price_amount_cents": 12990,
                  "iso_currency": "BRL",
                  "inventory_status": "AVAILABLE",
                  "deeplink": "https://store.steampowered.com/app/292030"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8083/api/v1/legacy-store/pricing/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResposta, MediaType.APPLICATION_JSON));

        Optional<OfertaLojaDTO> resultado = adapter.buscarOfertaPorGameId(1L);

        assertTrue(resultado.isPresent(), "Oferta adaptada deveria estar presente");
        OfertaLojaDTO oferta = resultado.get();
        assertEquals("Steam Partner Store", oferta.loja());
        assertEquals(129.90, oferta.preco(), 0.001, "Preço em centavos deve ser convertido para reais");
        assertEquals("BRL", oferta.moeda());
        assertTrue(oferta.emEstoque(), "Status AVAILABLE deve ser convertido para true");
        assertEquals("https://store.steampowered.com/app/292030", oferta.urlLoja());

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve tratar corretamente quando o produto estiver esgotado no sistema legado")
    void deveTratarProdutoEsgotado() {
        String jsonResposta = """
                {
                  "game_identifier": 2,
                  "external_sku": "SKU-ER-002",
                  "vendor_name": "Bandai Store",
                  "price_amount_cents": 25000,
                  "iso_currency": "BRL",
                  "inventory_status": "OUT_OF_STOCK",
                  "deeplink": "https://example.com"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8083/api/v1/legacy-store/pricing/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResposta, MediaType.APPLICATION_JSON));

        Optional<OfertaLojaDTO> resultado = adapter.buscarOfertaPorGameId(2L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().emEstoque(), "Status OUT_OF_STOCK deve ser convertido para false");
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o serviço legado retornar 404")
    void deveRetornarVazioQuandoNaoEncontrado() {
        mockServer.expect(requestTo("http://localhost:8083/api/v1/legacy-store/pricing/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<OfertaLojaDTO> resultado = adapter.buscarOfertaPorGameId(999L);

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o serviço legado estiver com erro interno (500)")
    void deveTratarErroServidorLegadoSemLancarExcecao() {
        mockServer.expect(requestTo("http://localhost:8083/api/v1/legacy-store/pricing/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        Optional<OfertaLojaDTO> resultado = adapter.buscarOfertaPorGameId(5L);

        assertTrue(resultado.isEmpty(), "Falha do serviço remoto deve ser tratada graciosamente");
        mockServer.verify();
    }
}
