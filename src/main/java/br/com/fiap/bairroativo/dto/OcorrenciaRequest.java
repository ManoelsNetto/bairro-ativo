package br.com.fiap.bairroativo.dto;

public record OcorrenciaRequest(String titulo, String descricao, String endereco, String referencia,
                                Long idBairro, Long idCategoria, Long idAutor) {
}
