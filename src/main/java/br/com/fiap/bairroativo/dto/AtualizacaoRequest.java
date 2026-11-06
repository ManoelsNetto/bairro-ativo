package br.com.fiap.bairroativo.dto;

public record AtualizacaoRequest(Long idAgente, String status, String comentario) {
}
