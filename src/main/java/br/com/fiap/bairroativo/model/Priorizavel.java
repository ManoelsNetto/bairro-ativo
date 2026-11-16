package br.com.fiap.bairroativo.model;

public interface Priorizavel {

    int getPontuacaoPrioridade();

    default NivelPrioridade getNivelPrioridade() {
        return NivelPrioridade.de(getPontuacaoPrioridade());
    }
}
