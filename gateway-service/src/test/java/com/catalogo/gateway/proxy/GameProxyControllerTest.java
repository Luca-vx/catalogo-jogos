package com.catalogo.gateway.proxy;

import com.catalogo.gateway.dto.GameDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameProxyControllerTest {

    @Mock
    private GameProxyService proxyService;

    @InjectMocks
    private GameProxyController controller;

    @Test
    @DisplayName("GET /game - deve retornar lista de jogos via proxy")
    void deveRetornarListaDeGames() {
        List<GameDTO> games = List.of(
                new GameDTO(1L, "The Witcher 3", "RPG", "PC", "CDPR", 9.8)
        );
        when(proxyService.buscarTodosGames()).thenReturn(ResponseEntity.ok(games));

        ResponseEntity<List<GameDTO>> response = controller.buscarTodosGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("The Witcher 3", response.getBody().get(0).titulo());
    }

    @Test
    @DisplayName("POST /game - deve criar novo jogo via proxy")
    void deveCriarGame() {
        GameDTO novo = new GameDTO(null, "Novo Jogo", "RPG", "PC", "Dev", 9.0);
        GameDTO salvo = new GameDTO(1L, "Novo Jogo", "RPG", "PC", "Dev", 9.0);
        when(proxyService.adicionarGame(novo)).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(salvo));

        ResponseEntity<GameDTO> response = controller.adicionarGame(novo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    @DisplayName("GET /game/{id} - deve buscar jogo por ID via proxy")
    void deveBuscarPorId() {
        GameDTO game = new GameDTO(1L, "The Witcher 3", "RPG", "PC", "CDPR", 9.8);
        when(proxyService.buscarPorId(1L)).thenReturn(ResponseEntity.ok(game));

        ResponseEntity<GameDTO> response = controller.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The Witcher 3", response.getBody().titulo());
    }

    @Test
    @DisplayName("PUT /game/{id} - deve atualizar jogo via proxy")
    void deveAtualizarGame() {
        GameDTO atualizado = new GameDTO(1L, "The Witcher 3 Updated", "RPG", "PC", "CDPR", 9.9);
        when(proxyService.atualizarGame(1L, atualizado)).thenReturn(ResponseEntity.ok(atualizado));

        ResponseEntity<GameDTO> response = controller.atualizarGame(1L, atualizado);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The Witcher 3 Updated", response.getBody().titulo());
    }

    @Test
    @DisplayName("DELETE /game/{id} - deve deletar jogo via proxy")
    void deveDeletarGame() {
        when(proxyService.deletarGame(1L)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Void> response = controller.deletarGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService).deletarGame(1L);
    }
}
