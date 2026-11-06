package br.com.fiap.bairroativo.dto;

/** O servidor não vem no corpo: é sempre quem está usando a plataforma. */
public record AtualizacaoRequest(String status, String comentario) {
}
