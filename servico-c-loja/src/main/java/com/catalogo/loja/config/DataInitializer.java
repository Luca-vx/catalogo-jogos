package com.catalogo.loja.config;

import com.catalogo.loja.entity.LegacyPricingItem;
import com.catalogo.loja.repository.LegacyPricingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initPricing(LegacyPricingRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new LegacyPricingItem(
                        1L,
                        "SKU-TW3-001",
                        "Steam Partner Store",
                        12990,
                        "BRL",
                        "AVAILABLE",
                        "https://store.steampowered.com/app/292030"
                ));

                repository.save(new LegacyPricingItem(
                        2L,
                        "SKU-ER-002",
                        "Bandai Namco Official",
                        22990,
                        "BRL",
                        "AVAILABLE",
                        "https://store.steampowered.com/app/1245620"
                ));

                repository.save(new LegacyPricingItem(
                        3L,
                        "SKU-GOW-003",
                        "PlayStation Direct",
                        29990,
                        "BRL",
                        "AVAILABLE",
                        "https://store.playstation.com/concept/10000856"
                ));
            }
        };
    }
}

