package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.Apoio;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.StatusOcorrencia;

import java.util.List;
import java.util.Set;

/** Ocorrência com seus apoios, histórico e os próximos status possíveis. */
public record OcorrenciaDetalheDTO(Ocorrencia ocorrencia, List<Apoio> apoios, List<Atualizacao> historico,
                                   Set<StatusOcorrencia> proximosStatus) {
}
