package br.com.fiap.bairroativo.dto;

/** O autor não vem no corpo: é sempre o cidadão que está usando a plataforma. */
public record OcorrenciaRequest(String titulo, String descricao, String endereco, String referencia,
                                Long idBairro, Long idCategoria) {
}
