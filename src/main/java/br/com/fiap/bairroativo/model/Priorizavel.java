package br.com.fiap.bairroativo.model;

/** Algo que pode ser ordenado por urgência de atendimento. */
public interface Priorizavel {

    int getPontuacaoPrioridade();

    default NivelPrioridade getNivelPrioridade() {
        return NivelPrioridade.de(getPontuacaoPrioridade());
    }
}
