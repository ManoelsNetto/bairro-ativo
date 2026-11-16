package br.com.fiap.bairroativo.dto;

public record ServidorRequest(String nome, String email, String telefone, String matricula, String cargo,
                              Long idOrgao) {
}
