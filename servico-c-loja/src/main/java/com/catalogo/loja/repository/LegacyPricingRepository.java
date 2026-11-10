package com.catalogo.loja.repository;

import com.catalogo.loja.entity.LegacyPricingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegacyPricingRepository extends JpaRepository<LegacyPricingItem, Long> {
    Optional<LegacyPricingItem> findByGameId(Long gameId);
}

