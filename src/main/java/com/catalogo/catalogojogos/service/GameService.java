package com.catalogo.catalogojogos.service;

import com.catalogo.catalogojogos.entity.Game;
import com.catalogo.catalogojogos.exception.GameNotFoundException;
import com.catalogo.catalogojogos.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Game adicionarGame(Game game) {
        return gameRepository.save(game);
    }

    public Game buscarId(Long id) {
        return gameRepository.findById(id).orElseThrow(
                () -> new GameNotFoundException(id)
        );
    }
    public List<Game> buscarTodosGames() {
        return gameRepository.findAll();
    }

    public Game atualizarGame(Long id, Game gameAtualizado) {
        Game gameExistente = buscarId(id);
        gameExistente.setTitulo(gameAtualizado.getTitulo());
        gameExistente.setGenero(gameAtualizado.getGenero());
        gameExistente.setPlataforma(gameAtualizado.getPlataforma());
        gameExistente.setDesenvolvedora(gameAtualizado.getDesenvolvedora());
        gameExistente.setNota(gameAtualizado.getNota());
        return gameRepository.save(gameExistente);
    }

    public void deletarGame(Long id) {
        Game game = buscarId(id);
        gameRepository.delete(game);
    }
}
