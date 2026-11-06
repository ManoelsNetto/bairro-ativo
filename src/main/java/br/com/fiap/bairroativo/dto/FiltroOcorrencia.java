package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.StatusOcorrencia;

/** Filtros opcionais da listagem de ocorrências (null = sem filtro). */
public record FiltroOcorrencia(StatusOcorrencia status, Long idBairro, Long idCategoria, String texto,
                               boolean somentePendentes) {

    public static FiltroOcorrencia pendentes() {
        return new FiltroOcorrencia(null, null, null, null, true);
    }
}
