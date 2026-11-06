package br.com.fiap.bairroativo.dto;

public record RelatorioCategoriaDTO(long idCategoria, String categoria, String orgao, int prazoDias, int total,
                                    int pendentes, int atrasadas, int resolvidasNoPrazo, int resolvidas) {
}
