package com.catalogo.gateway.aggregator;

import com.catalogo.gateway.dto.AvaliacaoResumoDTO;
import com.catalogo.gateway.dto.GameCompletoDTO;
import com.catalogo.gateway.dto.OfertaLojaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AggregatorControllerTest {

    @Mock
    private GameAggregatorService aggregatorService;

    @InjectMocks
    private AggregatorController controller;

    @Test
    @DisplayName("GET /game/{id}/completo - deve retornar 200 OK com visão agregada")
    void deveRetornarGameCompleto() {
        GameCompletoDTO completo = new GameCompletoDTO(
                1L,
                "The Witcher 3",
                "RPG",
                "PC",
                "CDPR",
                9.8,
                new AvaliacaoResumoDTO(1L, 9.5, 2, List.of("Muito bom!")),
                new OfertaLojaDTO("Steam Partner", 129.90, "BRL", true, "https://store.steampowered.com"),
                "INTEGRAÇÃO_COMPLETA_COM_SUCESSO"
        );

        when(aggregatorService.buscarGameCompleto(1L)).thenReturn(Optional.of(completo));

        ResponseEntity<GameCompletoDTO> response = controller.buscarGameCompleto(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The Witcher 3", response.getBody().titulo());
        assertEquals(9.5, response.getBody().avaliacoes().mediaNota());
        assertEquals("Steam Partner", response.getBody().melhorOferta().loja());
    }

    @Test
    @DisplayName("GET /game/{id}/completo - deve retornar 404 quando o jogo não existir")
    void deveRetornar404QuandoNaoExistir() {
        when(aggregatorService.buscarGameCompleto(999L)).thenReturn(Optional.empty());

        ResponseEntity<GameCompletoDTO> response = controller.buscarGameCompleto(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
