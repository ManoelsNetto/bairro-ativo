package br.com.fiap.bairroativo.dto;

/** Dados de cadastro/edição de usuário. idBairro vale para cidadão; orgao e matricula para agente. */
public record UsuarioRequest(String tipo, String nome, String email, String telefone,
                             Long idBairro, String orgao, String matricula) {
}
