package br.com.fiap.bairroativo.dto;

public record RelatorioBairroDTO(long idBairro, String bairro, String zona, int total, int pendentes,
                                 int resolvidas, int apoios, Double diasMediosResolucao) {
}
