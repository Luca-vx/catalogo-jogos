package com.catalogo.gateway.dto;

import java.util.List;

public record AvaliacaoResumoDTO(
        Long gameId,
        Double mediaNota,
        int totalAvaliacoes,
        List<String> comentarios
) {}

