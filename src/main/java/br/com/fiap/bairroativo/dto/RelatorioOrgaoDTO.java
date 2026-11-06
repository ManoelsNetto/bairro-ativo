package br.com.fiap.bairroativo.dto;

/** Situação da fila de cada órgão. */
public record RelatorioOrgaoDTO(long idOrgao, String orgao, String sigla, int pendentes, int atrasadas,
                                int resolvidas, Double diasMediosResolucao) {
}
