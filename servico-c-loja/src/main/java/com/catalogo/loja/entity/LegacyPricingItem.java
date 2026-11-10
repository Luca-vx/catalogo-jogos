package com.catalogo.loja.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "legacy_pricing")
public class LegacyPricingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long gameId;

    private String externalSku;
    private String vendorName;
    private Integer priceAmountCents;
    private String isoCurrency;
    private String inventoryStatus;
    private String deeplink;

    public LegacyPricingItem() {}

    public LegacyPricingItem(Long gameId, String externalSku, String vendorName,
                             Integer priceAmountCents, String isoCurrency,
                             String inventoryStatus, String deeplink) {
        this.gameId = gameId;
        this.externalSku = externalSku;
        this.vendorName = vendorName;
        this.priceAmountCents = priceAmountCents;
        this.isoCurrency = isoCurrency;
        this.inventoryStatus = inventoryStatus;
        this.deeplink = deeplink;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getExternalSku() {
        return externalSku;
    }

    public void setExternalSku(String externalSku) {
        this.externalSku = externalSku;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getPriceAmountCents() {
        return priceAmountCents;
    }

    public void setPriceAmountCents(Integer priceAmountCents) {
        this.priceAmountCents = priceAmountCents;
    }

    public String getIsoCurrency() {
        return isoCurrency;
    }

    public void setIsoCurrency(String isoCurrency) {
        this.isoCurrency = isoCurrency;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }
}

