package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.Apoio;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.StatusOcorrencia;

import java.util.List;
import java.util.Set;

/**
 * Ocorrência com apoios, histórico e o que o usuário atual pode fazer com ela
 * (as permissões vêm dos métodos polimórficos de Usuario).
 */
public record OcorrenciaDetalheDTO(Ocorrencia ocorrencia, List<Apoio> apoios, List<Atualizacao> historico,
                                   Set<StatusOcorrencia> proximosStatus, boolean podeEditar,
                                   boolean podeAtender, boolean podeApoiar, boolean jaApoiou) {
}
