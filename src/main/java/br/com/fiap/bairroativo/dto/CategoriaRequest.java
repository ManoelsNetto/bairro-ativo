package br.com.fiap.bairroativo.dto;

public record CategoriaRequest(String nome, String descricao, Integer prazoDias, Long idOrgao) {
}
