package br.com.fiap.bairroativo.dto;

/** Dados de cadastro e edição do cidadão. */
public record CidadaoRequest(String nome, String email, String telefone, Long idBairro) {
}
