package com.catalogo.loja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LegacyStorePricingDTO(
        @JsonProperty("game_identifier")
        Long gameIdentifier,

        @JsonProperty("external_sku")
        String externalSku,

        @JsonProperty("vendor_name")
        String vendorName,

        @JsonProperty("price_amount_cents")
        Integer priceAmountCents,

        @JsonProperty("iso_currency")
        String isoCurrency,

        @JsonProperty("inventory_status")
        String inventoryStatus,

        @JsonProperty("deeplink")
        String deeplink
) {}

