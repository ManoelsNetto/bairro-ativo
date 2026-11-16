package br.com.fiap.bairroativo.dto;

import br.com.fiap.bairroativo.model.TipoUsuario;

public record Sessao(TipoUsuario perfil, long idUsuario) {
}
