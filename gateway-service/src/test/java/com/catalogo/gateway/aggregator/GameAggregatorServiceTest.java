package com.catalogo.gateway.aggregator;

import com.catalogo.gateway.adapter.OfertaLojaPort;
import com.catalogo.gateway.client.AvaliacoesClient;
import com.catalogo.gateway.client.CatalogoClient;
import com.catalogo.gateway.dto.AvaliacaoResumoDTO;
import com.catalogo.gateway.dto.GameCompletoDTO;
import com.catalogo.gateway.dto.GameDTO;
import com.catalogo.gateway.dto.OfertaLojaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameAggregatorServiceTest {

    private OfertaLojaPort mockOfertaLojaPort;
    private AvaliacoesClient mockAvaliacoesClient;
    private CatalogoClient mockCatalogoClient;
    private GameAggregatorService aggregatorService;

    @BeforeEach
    void setUp() {
        this.mockOfertaLojaPort = mock(OfertaLojaPort.class);
        this.mockAvaliacoesClient = mock(AvaliacoesClient.class);
        this.mockCatalogoClient = mock(CatalogoClient.class);

        this.aggregatorService = new GameAggregatorService(
                mockOfertaLojaPort,
                mockAvaliacoesClient,
                mockCatalogoClient
        );
    }

    @Test
    @DisplayName("Deve agregar com sucesso informações dos três serviços (A, B e C)")
    void deveAgregarInformacoesComSucesso() {
        Long gameId = 1L;

        when(mockCatalogoClient.buscarPorId(gameId))
                .thenReturn(new GameDTO(1L, "The Witcher 3", "RPG", "PC", "CDPR", 9.8));

        when(mockAvaliacoesClient.buscarResumo(eq(gameId), anyList()))
                .thenReturn(new AvaliacaoResumoDTO(1L, 9.7, 2, List.of("Obra prima!", "Muito bom.")));

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

        verify(mockCatalogoClient).buscarPorId(gameId);
        verify(mockAvaliacoesClient).buscarResumo(eq(gameId), anyList());
        verify(mockOfertaLojaPort).buscarOfertaPorGameId(gameId);
    }

    @Test
    @DisplayName("Deve aplicar degradação graciosa quando o Serviço B ou C falhar")
    void deveAplicarDegradacaoGraciosaEmFalhasSecundarias() {
        Long gameId = 2L;

        when(mockCatalogoClient.buscarPorId(gameId))
                .thenReturn(new GameDTO(2L, "Elden Ring", "Action RPG", "PS5", "FromSoftware", 9.6));

        when(mockAvaliacoesClient.buscarResumo(eq(gameId), anyList()))
                .thenAnswer(invocation -> {
                    List<String> avisos = invocation.getArgument(1);
                    avisos.add("Serviço de avaliações temporariamente indisponível ou sem dados");
                    return new AvaliacaoResumoDTO(gameId, 0.0, 0, List.of());
                });

        when(mockOfertaLojaPort.buscarOfertaPorGameId(gameId)).thenReturn(Optional.empty());

        Optional<GameCompletoDTO> resultado = aggregatorService.buscarGameCompleto(gameId);

        assertTrue(resultado.isPresent(), "Mesmo com falhas nos serviços secundários, deve retornar os dados do jogo");
        GameCompletoDTO completo = resultado.get();
        assertEquals("Elden Ring", completo.titulo());
        assertNull(completo.melhorOferta());
        assertEquals(0, completo.avaliacoes().totalAvaliacoes());
        assertTrue(completo.statusIntegracao().contains("INTEGRAÇÃO_PARCIAL"));
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o jogo não existir no Serviço A")
    void deveRetornarVazioQuandoGameNaoExistir() {
        Long gameId = 999L;

        when(mockCatalogoClient.buscarPorId(gameId)).thenReturn(null);

        Optional<GameCompletoDTO> resultado = aggregatorService.buscarGameCompleto(gameId);

        assertTrue(resultado.isEmpty());
        verify(mockCatalogoClient).buscarPorId(gameId);
    }
}
