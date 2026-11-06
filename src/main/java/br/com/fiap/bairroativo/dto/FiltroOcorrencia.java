package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.StatusOcorrencia;

/** Filtros opcionais da listagem de ocorrências (null = sem filtro). */
public record FiltroOcorrencia(StatusOcorrencia status, Long idBairro, Long idCategoria, String texto,
                               boolean somentePendentes, Long idAutor, Long idApoiador, Long idOrgao) {

    public static FiltroOcorrencia simples(StatusOcorrencia status, Long idBairro, Long idCategoria, String texto) {
        return new FiltroOcorrencia(status, idBairro, idCategoria, texto, false, null, null, null);
    }

    public static FiltroOcorrencia pendentes() {
        return new FiltroOcorrencia(null, null, null, null, true, null, null, null);
    }

    public static FiltroOcorrencia pendentesDoOrgao(long idOrgao) {
        return new FiltroOcorrencia(null, null, null, null, true, null, null, idOrgao);
    }

    public static FiltroOcorrencia doAutor(long idCidadao) {
        return new FiltroOcorrencia(null, null, null, null, false, idCidadao, null, null);
    }

    public static FiltroOcorrencia apoiadasPor(long idCidadao) {
        return new FiltroOcorrencia(null, null, null, null, false, null, idCidadao, null);
    }
}
