package br.com.fiap.bairroativo.dto;

public record RelatorioCategoriaDTO(long idCategoria, String categoria, int prazoDias, int total,
                                    int pendentes, int atrasadas, int resolvidasNoPrazo, int resolvidas) {
}
