package com.catalogo.gateway.dto;

public record GameDTO(
        Long id,
        String titulo,
        String genero,
        String plataforma,
        String desenvolvedora,
        Double nota
) {}

