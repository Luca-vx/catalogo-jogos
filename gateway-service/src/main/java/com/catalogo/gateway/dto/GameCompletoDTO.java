package com.catalogo.gateway.dto;

public record GameCompletoDTO(
        Long id,
        String titulo,
        String genero,
        String plataforma,
        String desenvolvedora,
        Double notaOriginal,
        AvaliacaoResumoDTO avaliacoes,
        OfertaLojaDTO melhorOferta,
        String statusIntegracao
) {}

