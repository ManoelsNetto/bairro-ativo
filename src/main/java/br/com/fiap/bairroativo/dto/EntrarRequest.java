package br.com.fiap.bairroativo.dto;

/** Entrada de demonstração: perfil (CIDADAO ou SERVIDOR) e e-mail cadastrado. */
public record EntrarRequest(String perfil, String email) {
}
