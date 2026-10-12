package com.catalogo.catalogojogos.service;

import com.catalogo.catalogojogos.entity.Game;
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
        return gameRepository.findById(id).get();
    }
    public List<Game> buscarTodosGames() {
        return gameRepository.findAll();
    }
}
