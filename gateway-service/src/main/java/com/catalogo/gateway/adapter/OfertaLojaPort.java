package com.catalogo.gateway.adapter;

import com.catalogo.gateway.dto.OfertaLojaDTO;
import java.util.Optional;

/**
 * Porta de integração (Target) para consulta de ofertas e preços de lojas.
 * Define o contrato canônico esperado pelo domínio da aplicação,
 * desacoplando o Aggregator de sistemas legados ou provedores externos.
 */
public interface OfertaLojaPort {

    /**
     * Obtém a melhor oferta de preço para um jogo, já convertida para o modelo canônico.
     *
     * @param gameId Identificador do jogo no catálogo
     * @return Oferta padronizada ou Optional.empty() caso não haja oferta ou ocorra indisponibilidade
     */
    Optional<OfertaLojaDTO> buscarOfertaPorGameId(Long gameId);
}

