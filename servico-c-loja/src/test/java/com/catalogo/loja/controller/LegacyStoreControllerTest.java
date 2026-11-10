package com.catalogo.loja.controller;

import com.catalogo.loja.dto.LegacyStorePricingDTO;
import com.catalogo.loja.entity.LegacyPricingItem;
import com.catalogo.loja.repository.LegacyPricingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyStoreControllerTest {

    @Mock
    private LegacyPricingRepository repository;

    @InjectMocks
    private LegacyStoreController controller;

    @Test
    @DisplayName("Deve retornar 200 OK com dados legados quando o preço do jogo for encontrado")
    void deveRetornarPrecoComSucesso() {
        LegacyPricingItem item = new LegacyPricingItem(
                1L,
                "SKU-100",
                "Epic Store Partner",
                14990,
                "BRL",
                "AVAILABLE",
                "https://epicgames.com"
        );
        when(repository.findByGameId(1L)).thenReturn(Optional.of(item));

        ResponseEntity<LegacyStorePricingDTO> response = controller.getPricingByGameId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SKU-100", response.getBody().externalSku());
        assertEquals(14990, response.getBody().priceAmountCents());
        assertEquals("AVAILABLE", response.getBody().inventoryStatus());
    }

    @Test
    @DisplayName("Deve retornar 404 NOT FOUND quando o preço do jogo não existir")
    void deveRetornar404QuandoNaoExistir() {
        when(repository.findByGameId(999L)).thenReturn(Optional.empty());

        ResponseEntity<LegacyStorePricingDTO> response = controller.getPricingByGameId(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Deve salvar novo preço legado e retornar 201 CREATED")
    void deveSalvarNovoPreco() {
        LegacyStorePricingDTO dto = new LegacyStorePricingDTO(
                5L,
                "SKU-500",
                "GOG Galaxy Store",
                9990,
                "BRL",
                "AVAILABLE",
                "https://gog.com"
        );

        when(repository.findByGameId(5L)).thenReturn(Optional.empty());
        when(repository.save(any(LegacyPricingItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<LegacyStorePricingDTO> response = controller.savePricing(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().gameIdentifier());
        assertEquals(9990, response.getBody().priceAmountCents());
    }
}

