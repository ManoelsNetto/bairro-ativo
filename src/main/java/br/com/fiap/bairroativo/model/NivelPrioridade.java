package br.com.fiap.bairroativo.model;

/** Faixas de prioridade calculadas a partir da pontuação de uma ocorrência. */
public enum NivelPrioridade {
    BAIXA, MEDIA, ALTA;

    public static final int MINIMO_MEDIA = 15;
    public static final int MINIMO_ALTA = 35;

    public static NivelPrioridade de(int pontuacao) {
        if (pontuacao >= MINIMO_ALTA) {
            return ALTA;
        }
        return pontuacao >= MINIMO_MEDIA ? MEDIA : BAIXA;
    }
}
