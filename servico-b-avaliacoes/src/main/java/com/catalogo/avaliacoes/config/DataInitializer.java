package com.catalogo.avaliacoes.config;

import com.catalogo.avaliacoes.entity.Avaliacao;
import com.catalogo.avaliacoes.repository.AvaliacaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAvaliacoes(AvaliacaoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // The Witcher 3 (gameId = 1)
                repository.save(new Avaliacao(1L, "GeraltDeRivia", 10.0, "Obra de arte impecável! Trilha sonora e narrativa incríveis."));
                repository.save(new Avaliacao(1L, "YenneferV", 9.5, "Excelente combate e missões secundárias profundas."));

                // Elden Ring (gameId = 2)
                repository.save(new Avaliacao(2L, "TarnishedOne", 9.8, "Mundo aberto revolucionário e chefes desafiadores!"));
                repository.save(new Avaliacao(2L, "RanniTheWitch", 9.7, "Direção de arte estonteante. Vale cada morte."));

                // God of War Ragnarok (gameId = 3)
                repository.save(new Avaliacao(3L, "KratosSparta", 9.5, "História emocionante sobre paternidade e destino."));
            }
        };
    }
}

