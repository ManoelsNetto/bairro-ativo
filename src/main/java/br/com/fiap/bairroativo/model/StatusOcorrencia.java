package br.com.fiap.bairroativo.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de uma ocorrência. Cada status conhece para quais outros pode avançar:
 * ABERTA -> EM_ANALISE -> EM_ANDAMENTO -> RESOLVIDA, e qualquer status pendente pode ser CANCELADA.
 */
public enum StatusOcorrencia {
    ABERTA("Aberta"),
    EM_ANALISE("Em análise"),
    EM_ANDAMENTO("Em andamento"),
    RESOLVIDA("Resolvida"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOcorrencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPendente() {
        return this == ABERTA || this == EM_ANALISE || this == EM_ANDAMENTO;
    }

    public boolean isFinal() {
        return !isPendente();
    }

    public Set<StatusOcorrencia> proximosPermitidos() {
        return switch (this) {
            case ABERTA -> EnumSet.of(EM_ANALISE, EM_ANDAMENTO, RESOLVIDA, CANCELADA);
            case EM_ANALISE -> EnumSet.of(EM_ANDAMENTO, RESOLVIDA, CANCELADA);
            case EM_ANDAMENTO -> EnumSet.of(RESOLVIDA, CANCELADA);
            case RESOLVIDA, CANCELADA -> EnumSet.noneOf(StatusOcorrencia.class);
        };
    }

    public boolean podeMudarPara(StatusOcorrencia novo) {
        return proximosPermitidos().contains(novo);
    }
}
