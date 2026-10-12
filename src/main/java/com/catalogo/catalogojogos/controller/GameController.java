package com.catalogo.catalogojogos.controller;

import com.catalogo.catalogojogos.entity.Game;
import com.catalogo.catalogojogos.repository.GameRepository;
import com.catalogo.catalogojogos.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping
    public Game adicionarGame(@RequestBody Game game) {
        return gameService.adicionarGame(game);
    }

    @GetMapping
    public List<Game> buscarTodosGames() {
        return gameService.buscarTodosGames();
    }

    @GetMapping("/{id}")
    public Game buscarId(@PathVariable Long id) {
        return gameService.buscarId(id);
    }
}
