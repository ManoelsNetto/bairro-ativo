package br.com.fiap.bairroativo.dto;

public record RelatorioOrgaoDTO(long idOrgao, String orgao, String sigla, int pendentes, int atrasadas,
                                int resolvidas, Double diasMediosResolucao) {
}
