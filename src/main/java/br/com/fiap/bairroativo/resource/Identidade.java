package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.Sessao;
import br.com.fiap.bairroativo.exception.NaoAutenticadoException;
import br.com.fiap.bairroativo.model.TipoUsuario;

import java.util.Locale;

/**
 * Lê quem está usando a plataforma a partir dos cabeçalhos que o front envia depois de "Entrar":
 * X-Perfil (CIDADAO ou SERVIDOR) e X-Usuario (id). Sem cabeçalhos = visitante.
 */
public final class Identidade {

    public static final String PERFIL = "X-Perfil";
    public static final String USUARIO = "X-Usuario";

    private Identidade() {
    }

    public static Sessao ler(String perfil, String idUsuario) {
        boolean semPerfil = perfil == null || perfil.isBlank();
        boolean semId = idUsuario == null || idUsuario.isBlank();
        if (semPerfil && semId) {
            return null;
        }
        try {
            return new Sessao(TipoUsuario.valueOf(perfil.trim().toUpperCase(Locale.ROOT)),
                    Long.parseLong(idUsuario.trim()));
        } catch (RuntimeException e) {
            throw new NaoAutenticadoException("Sessão inválida. Entre novamente.");
        }
    }
}
