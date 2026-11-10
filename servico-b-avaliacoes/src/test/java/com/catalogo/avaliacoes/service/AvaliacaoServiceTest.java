package com.catalogo.avaliacoes.service;

import com.catalogo.avaliacoes.dto.AvaliacaoResumoResponse;
import com.catalogo.avaliacoes.entity.Avaliacao;
import com.catalogo.avaliacoes.repository.AvaliacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository repository;

    @InjectMocks
    private AvaliacaoService service;

    @Test
    @DisplayName("Deve calcular resumo com média de notas e lista de comentários")
    void deveCalcularResumoComMediaEComentarios() {
        Long gameId = 1L;
        List<Avaliacao> lista = List.of(
                new Avaliacao(gameId, "User1", 10.0, "Excelente!"),
                new Avaliacao(gameId, "User2", 8.0, "Muito bom."),
                new Avaliacao(gameId, "User3", 9.0, "Gostei bastante.")
        );

        when(repository.findByGameId(gameId)).thenReturn(lista);

        AvaliacaoResumoResponse resumo = service.obterResumoPorJogo(gameId);

        assertEquals(gameId, resumo.gameId());
        assertEquals(9.0, resumo.mediaNota(), 0.01);
        assertEquals(3, resumo.totalAvaliacoes());
        assertEquals(3, resumo.comentarios().size());
        assertTrue(resumo.comentarios().contains("Excelente!"));
    }

    @Test
    @DisplayName("Deve retornar média 0 e lista vazia para jogo sem avaliações")
    void deveRetornarResumoVazioParaJogoSemAvaliacoes() {
        Long gameId = 99L;
        when(repository.findByGameId(gameId)).thenReturn(List.of());

        AvaliacaoResumoResponse resumo = service.obterResumoPorJogo(gameId);

        assertEquals(0.0, resumo.mediaNota());
        assertEquals(0, resumo.totalAvaliacoes());
        assertTrue(resumo.comentarios().isEmpty());
    }
}

