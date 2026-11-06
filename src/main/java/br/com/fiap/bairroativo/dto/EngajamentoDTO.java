package br.com.fiap.bairroativo.dto;

/** Participação de cada cidadão: ocorrências registradas e apoios dados. */
public record EngajamentoDTO(long idCidadao, String nome, String bairro, int ocorrenciasRegistradas,
                             int apoiosDados) {
}
