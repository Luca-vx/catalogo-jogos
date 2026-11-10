package com.catalogo.gateway.proxy;

import com.catalogo.gateway.dto.GameDTO;
import com.catalogo.gateway.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GameProxyServiceTest {

    private MockRestServiceServer mockServer;
    private GameProxyService proxyService;

    private final String catalogoUrl = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        this.proxyService = new GameProxyService(builder.build(), catalogoUrl);
    }

    @Test
    @DisplayName("Deve encaminhar requisição GET de listagem para o Serviço A")
    void deveEncaminharBuscarTodos() {
        String jsonResposta = """
                [
                  {"id": 1, "titulo": "Jogo 1", "genero": "Ação", "plataforma": "PC", "desenvolvedora": "Dev 1", "nota": 9.0},
                  {"id": 2, "titulo": "Jogo 2", "genero": "RPG", "plataforma": "PS5", "desenvolvedora": "Dev 2", "nota": 8.5}
                ]
                """;

        mockServer.expect(requestTo("http://localhost:8081/game"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResposta, MediaType.APPLICATION_JSON));

        ResponseEntity<List<GameDTO>> response = proxyService.buscarTodosGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Jogo 1", response.getBody().get(0).titulo());

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve encaminhar requisição POST de criação para o Serviço A")
    void deveEncaminharAdicionarGame() {
        GameDTO novoGame = new GameDTO(null, "Novo Jogo", "Aventura", "PC", "Indie Dev", 9.2);
        String jsonCriado = """
                {"id": 10, "titulo": "Novo Jogo", "genero": "Aventura", "plataforma": "PC", "desenvolvedora": "Indie Dev", "nota": 9.2}
                """;

        mockServer.expect(requestTo("http://localhost:8081/game"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).body(jsonCriado).contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<GameDTO> response = proxyService.adicionarGame(novoGame);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().id());

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve propagar 404 quando o jogo não for encontrado no Serviço A")
    void devePropagar404QuandoNaoEncontrado() {
        mockServer.expect(requestTo("http://localhost:8081/game/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity<GameDTO> response = proxyService.buscarPorId(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar ServiceUnavailableException quando o Serviço A estiver inacessível")
    void deveTratarIndisponibilidadeDoServico() {
        mockServer.expect(requestTo("http://localhost:8081/game/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond((request) -> {
                    throw new org.springframework.web.client.ResourceAccessException("Falha de conexão com localhost:8081");
                });

        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> {
            proxyService.buscarPorId(1L);
        });

        assertTrue(ex.getMessage().contains("indisponível"));
        mockServer.verify();
    }
}
