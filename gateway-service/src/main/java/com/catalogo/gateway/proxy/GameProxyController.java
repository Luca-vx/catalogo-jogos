package com.catalogo.gateway.proxy;

import com.catalogo.gateway.dto.GameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador Proxy na rota /game (Porta 8080).
 *
 * Ponto de entrada unificado para clientes externos.
 * O cliente realiza todas as operações CRUD no endereço http://localhost:8080/game
 * sem conhecimento da porta interna (8081).
 */
@RestController
@RequestMapping("/game")
public class GameProxyController {

    @Autowired
    private GameProxyService proxyService;

    @PostMapping
    public ResponseEntity<GameDTO> adicionarGame(@RequestBody GameDTO game) {
        return proxyService.adicionarGame(game);
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> buscarTodosGames() {
        return proxyService.buscarTodosGames();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> buscarPorId(@PathVariable Long id) {
        return proxyService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameDTO> atualizarGame(@PathVariable Long id, @RequestBody GameDTO game) {
        return proxyService.atualizarGame(id, game);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGame(@PathVariable Long id) {
        return proxyService.deletarGame(id);
    }
}

