package com.catalogo.loja.controller;

import com.catalogo.loja.dto.LegacyStorePricingDTO;
import com.catalogo.loja.entity.LegacyPricingItem;
import com.catalogo.loja.repository.LegacyPricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/legacy-store")
public class LegacyStoreController {

    @Autowired
    private LegacyPricingRepository repository;

    @GetMapping("/pricing/{gameId}")
    public ResponseEntity<LegacyStorePricingDTO> getPricingByGameId(@PathVariable Long gameId) {
        return repository.findByGameId(gameId)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/pricing")
    public ResponseEntity<LegacyStorePricingDTO> savePricing(@RequestBody LegacyStorePricingDTO dto) {
        LegacyPricingItem item = repository.findByGameId(dto.gameIdentifier())
                .orElse(new LegacyPricingItem());

        item.setGameId(dto.gameIdentifier());
        item.setExternalSku(dto.externalSku());
        item.setVendorName(dto.vendorName());
        item.setPriceAmountCents(dto.priceAmountCents());
        item.setIsoCurrency(dto.isoCurrency());
        item.setInventoryStatus(dto.inventoryStatus());
        item.setDeeplink(dto.deeplink());

        LegacyPricingItem saved = repository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    private LegacyStorePricingDTO toDTO(LegacyPricingItem item) {
        return new LegacyStorePricingDTO(
                item.getGameId(),
                item.getExternalSku(),
                item.getVendorName(),
                item.getPriceAmountCents(),
                item.getIsoCurrency(),
                item.getInventoryStatus(),
                item.getDeeplink()
        );
    }
}

