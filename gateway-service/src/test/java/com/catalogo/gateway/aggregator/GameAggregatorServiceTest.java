package com.catalogo.gateway.aggregator;

import com.catalogo.gateway.adapter.OfertaLojaPort;
import com.catalogo.gateway.dto.GameCompletoDTO;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GameAggregatorServiceTest {

    private MockRestServiceServer mockServer;
    private OfertaLojaPort mockOfertaLojaPort;
    private GameAggregatorService aggregatorService;

    private final String catalogoUrl = "http://localhost:8081";
    private final String avaliacoesUrl = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        this.mockOfertaLojaPort = mock(OfertaLojaPort.class);

        this.aggregatorService = new GameAggregatorService(
                builder.build(),
                mockOfertaLojaPort,
                catalogoUrl,
                avaliacoesUrl
        );
    }

    @Test
    @DisplayName("Deve agregar com sucesso informações dos três serviços (A, B e C)")
    void deveAgregarInformacoesComSucesso() {
        Long gameId = 1L;

        // Mock do Serviço A (Catálogo)
        String jsonGame = """
                {"id": 1, "titulo": "The Witcher 3", "genero": "RPG", "plataforma": "PC", "desenvolvedora": "CDPR", "nota": 9.8}
                """;
        mockServer.expect(requestTo("http://localhost:8081/game/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonGame, MediaType.APPLICATION_JSON));

        // Mock do Serviço B (Avaliações)
        String jsonAvaliacoes = """
                {
                  "gameId": 1,
                  "mediaNota": 9.7,
                  "totalAvaliacoes": 2,
                  "comentarios": ["Obra prima!", "Muito bom."]
                }
                """;
        mockServer.expect(requestTo("http://localhost:8082/avaliacoes/game/1/resumo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonAvaliacoes, MediaType.APPLICATION_JSON));

        // Mock do Serviço C via Adapter
        OfertaLojaDTO oferta = new OfertaLojaDTO("Steam Partner", 129.90, "BRL", true, "https://store.steampowered.com");
        when(mockOfertaLojaPort.buscarOfertaPorGameId(gameId)).thenReturn(Optional.of(oferta));

        Optional<GameCompletoDTO> resultado = aggregatorService.buscarGameCompleto(gameId);

        assertTrue(resultado.isPresent());
        GameCompletoDTO completo = resultado.get();
        assertEquals("The Witcher 3", completo.titulo());
        assertEquals(9.8, completo.notaOriginal());
        assertNotNull(completo.avaliacoes());
        assertEquals(9.7, completo.avaliacoes().mediaNota());
        assertEquals(2, completo.avaliacoes().totalAvaliacoes());
        assertNotNull(completo.melhorOferta());
        assertEquals("Steam Partner", completo.melhorOferta().loja());
        assertEquals(129.90, completo.melhorOferta().preco());
        assertTrue(completo.statusIntegracao().contains("INTEGRAÇÃO_COMPLETA_COM_SUCESSO"));

        mockServer.verify();
        verify(mockOfertaLojaPort).buscarOfertaPorGameId(gameId);
    }

    @Test
    @DisplayName("Deve aplicar degradação graciosa quando o Serviço B ou C falhar")
    void deveAplicarDegradacaoGraciosaEmFalhasSecundarias() {
        Long gameId = 2L;

        // Mock do Serviço A respondendo com sucesso
        String jsonGame = """
                {"id": 2, "titulo": "Elden Ring", "genero": "Action RPG", "plataforma": "PS5", "desenvolvedora": "FromSoftware", "nota": 9.6}
                """;
        mockServer.expect(requestTo("http://localhost:8081/game/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonGame, MediaType.APPLICATION_JSON));

        // Mock do Serviço B indisponível (retorna 500)
        mockServer.expect(requestTo("http://localhost:8082/avaliacoes/game/2/resumo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Mock do Serviço C sem oferta
        when(mockOfertaLojaPort.buscarOfertaPorGameId(gameId)).thenReturn(Optional.empty());

        Optional<GameCompletoDTO> resultado = aggregatorService.buscarGameCompleto(gameId);

        assertTrue(resultado.isPresent(), "Mesmo com falhas nos serviços secundários, deve retornar os dados do jogo");
        GameCompletoDTO completo = resultado.get();
        assertEquals("Elden Ring", completo.titulo());
        assertNull(completo.melhorOferta());
        assertEquals(0, completo.avaliacoes().totalAvaliacoes());
        assertTrue(completo.statusIntegracao().contains("INTEGRAÇÃO_PARCIAL"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o jogo não existir no Serviço A")
    void deveRetornarVazioQuandoGameNaoExistir() {
        Long gameId = 999L;

        mockServer.expect(requestTo("http://localhost:8081/game/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<GameCompletoDTO> resultado = aggregatorService.buscarGameCompleto(gameId);

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }
}
