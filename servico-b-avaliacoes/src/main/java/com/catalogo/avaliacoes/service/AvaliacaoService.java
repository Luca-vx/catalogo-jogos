package com.catalogo.avaliacoes.service;

import com.catalogo.avaliacoes.dto.AvaliacaoResumoResponse;
import com.catalogo.avaliacoes.entity.Avaliacao;
import com.catalogo.avaliacoes.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository repository;

    public Avaliacao adicionarAvaliacao(Avaliacao avaliacao) {
        return repository.save(avaliacao);
    }

    public List<Avaliacao> buscarPorJogo(Long gameId) {
        return repository.findByGameId(gameId);
    }

    public AvaliacaoResumoResponse obterResumoPorJogo(Long gameId) {
        List<Avaliacao> avaliacoes = repository.findByGameId(gameId);
        if (avaliacoes.isEmpty()) {
            return new AvaliacaoResumoResponse(gameId, 0.0, 0, List.of());
        }

        double media = avaliacoes.stream()
                .filter(a -> a.getNota() != null)
                .mapToDouble(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        List<String> comentarios = avaliacoes.stream()
                .map(Avaliacao::getComentario)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        double mediaArredondada = Math.round(media * 10.0) / 10.0;

        return new AvaliacaoResumoResponse(gameId, mediaArredondada, avaliacoes.size(), comentarios);
    }
}

