package com.catalogo.avaliacoes.dto;

import java.util.List;

public record AvaliacaoResumoResponse(
        Long gameId,
        Double mediaNota,
        int totalAvaliacoes,
        List<String> comentarios
) {}

