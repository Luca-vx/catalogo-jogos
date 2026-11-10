package com.catalogo.gateway.dto;

public record OfertaLojaDTO(
        String loja,
        Double preco,
        String moeda,
        boolean emEstoque,
        String urlLoja
) {}

