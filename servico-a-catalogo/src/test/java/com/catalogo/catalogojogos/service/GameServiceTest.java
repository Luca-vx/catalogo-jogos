package com.catalogo.catalogojogos.service;

import com.catalogo.catalogojogos.entity.Game;
import com.catalogo.catalogojogos.exception.GameNotFoundException;
import com.catalogo.catalogojogos.repository.GameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("Deve salvar e retornar um novo jogo")
    void deveSalvarGame() {
        Game game = new Game("Hollow Knight", "Metroidvania", "PC", "Team Cherry", 9.5);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game salvo = gameService.adicionarGame(game);

        assertNotNull(salvo);
        assertEquals("Hollow Knight", salvo.getTitulo());
        verify(gameRepository).save(game);
    }

    @Test
    @DisplayName("Deve buscar jogo por ID com sucesso")
    void deveBuscarPorId() {
        Game game = new Game("Celeste", "Platformer", "Switch", "Maddy Makes Games", 9.3);
        game.setId(1L);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        Game encontrado = gameService.buscarId(1L);

        assertNotNull(encontrado);
        assertEquals("Celeste", encontrado.getTitulo());
    }

    @Test
    @DisplayName("Deve lançar GameNotFoundException ao buscar ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> {
            gameService.buscarId(99L);
        });
    }

    @Test
    @DisplayName("Deve listar todos os jogos")
    void deveListarTodos() {
        List<Game> lista = List.of(
                new Game("Game 1", "RPG", "PC", "Dev 1", 8.0),
                new Game("Game 2", "Ação", "PS5", "Dev 2", 9.0)
        );
        when(gameRepository.findAll()).thenReturn(lista);

        List<Game> resultado = gameService.buscarTodosGames();

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Deve atualizar um jogo existente")
    void deveAtualizarGame() {
        Game existente = new Game("Titulo Antigo", "RPG", "PC", "Dev", 7.0);
        existente.setId(1L);
        Game atualizado = new Game("Titulo Novo", "RPG", "PC", "Dev", 8.5);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game resultado = gameService.atualizarGame(1L, atualizado);

        assertEquals("Titulo Novo", resultado.getTitulo());
        assertEquals(8.5, resultado.getNota());
    }

    @Test
    @DisplayName("Deve deletar um jogo existente")
    void deveDeletarGame() {
        Game existente = new Game("Jogo Para Deletar", "Aventura", "PC", "Dev", 6.0);
        existente.setId(5L);

        when(gameRepository.findById(5L)).thenReturn(Optional.of(existente));
        doNothing().when(gameRepository).delete(existente);

        assertDoesNotThrow(() -> gameService.deletarGame(5L));
        verify(gameRepository).delete(existente);
    }
}

