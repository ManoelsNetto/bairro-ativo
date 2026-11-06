package br.com.fiap.bairroativo.dto;

/** Dados de cadastro e edição do servidor público. */
public record ServidorRequest(String nome, String email, String telefone, String matricula, String cargo,
                              Long idOrgao) {
}
