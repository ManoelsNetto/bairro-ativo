package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.TipoUsuario;

/** Quem está usando a plataforma, enviado pelo front nos cabeçalhos X-Perfil e X-Usuario. */
public record Sessao(TipoUsuario perfil, long idUsuario) {
}
