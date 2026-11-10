package com.catalogo.catalogojogos.config;

import com.catalogo.catalogojogos.entity.Game;
import com.catalogo.catalogojogos.repository.GameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(GameRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Game("The Witcher 3: Wild Hunt", "RPG", "PC / PS5 / Xbox Series", "CD Projekt Red", 9.8));
                repository.save(new Game("Elden Ring", "Action RPG", "PC / PS5 / Xbox Series", "FromSoftware", 9.6));
                repository.save(new Game("God of War Ragnarok", "Ação / Aventura", "PS4 / PS5", "Santa Monica Studio", 9.4));
            }
        };
    }
}

