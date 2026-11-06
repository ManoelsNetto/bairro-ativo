package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.CidadaoDAO;
import br.com.fiap.bairroativo.dao.ServidorDAO;
import br.com.fiap.bairroativo.dto.ContaExemploDTO;
import br.com.fiap.bairroativo.dto.EntrarRequest;
import br.com.fiap.bairroativo.dto.Sessao;
import br.com.fiap.bairroativo.exception.NaoAutenticadoException;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.SemPermissaoException;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.ServidorPublico;
import br.com.fiap.bairroativo.model.TipoUsuario;
import br.com.fiap.bairroativo.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Identifica quem está usando a plataforma.
 * MVP: a entrada é de demonstração, só com o e-mail cadastrado (sem senha).
 * Numa versão real a identificação viria do gov.br.
 */
public class SessaoService {

    private static final int EXEMPLOS_CIDADAOS = 4;

    private final CidadaoDAO cidadaoDAO;
    private final ServidorDAO servidorDAO;

    public SessaoService(CidadaoDAO cidadaoDAO, ServidorDAO servidorDAO) {
        this.cidadaoDAO = cidadaoDAO;
        this.servidorDAO = servidorDAO;
    }

    public Usuario entrar(EntrarRequest req) {
        Validador.corpo(req);
        TipoUsuario perfil = Validador.opcao(req.perfil(), TipoUsuario.class, "o perfil");
        String email = Validador.email(req.email());
        Optional<? extends Usuario> usuario = perfil == TipoUsuario.CIDADAO
                ? cidadaoDAO.buscarPorEmail(email) : servidorDAO.buscarPorEmail(email);
        return usuario.orElseThrow(() -> new NaoEncontradoException(perfil == TipoUsuario.CIDADAO
                ? "Não encontramos um cidadão com este e-mail. Que tal criar sua conta?"
                : "Não encontramos um servidor com este e-mail."));
    }

    /** Usuário da sessão, se houver (visitantes podem consultar sem entrar). */
    public Optional<Usuario> usuario(Sessao sessao) {
        if (sessao == null) {
            return Optional.empty();
        }
        Optional<? extends Usuario> encontrado = sessao.perfil() == TipoUsuario.CIDADAO
                ? cidadaoDAO.buscarPorId(sessao.idUsuario()) : servidorDAO.buscarPorId(sessao.idUsuario());
        return Optional.ofNullable(encontrado.orElse(null));
    }

    public Usuario exigirUsuario(Sessao sessao) {
        return usuario(sessao).orElseThrow(() -> new NaoAutenticadoException("Entre na plataforma para continuar."));
    }

    public Cidadao exigirCidadao(Sessao sessao, String acao) {
        Usuario usuario = usuario(sessao)
                .orElseThrow(() -> new NaoAutenticadoException("Entre como cidadão para " + acao + "."));
        if (!(usuario instanceof Cidadao cidadao)) {
            throw new SemPermissaoException("Somente cidadãos podem " + acao + ".");
        }
        return cidadao;
    }

    public ServidorPublico exigirServidor(Sessao sessao, String acao) {
        Usuario usuario = usuario(sessao)
                .orElseThrow(() -> new NaoAutenticadoException("Entre como servidor público para " + acao + "."));
        if (!(usuario instanceof ServidorPublico servidor)) {
            throw new SemPermissaoException("Somente servidores públicos podem " + acao + ".");
        }
        return servidor;
    }

    /** Contas da carga inicial para testar os dois perfis na demonstração. */
    public List<ContaExemploDTO> exemplos() {
        List<ContaExemploDTO> contas = new ArrayList<>();
        cidadaoDAO.listar().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .limit(EXEMPLOS_CIDADAOS)
                .forEach(c -> contas.add(new ContaExemploDTO(c.getTipo().name(), c.getNome(), c.getEmail(),
                        c.getDescricaoPerfil())));
        servidorDAO.listar().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .forEach(s -> contas.add(new ContaExemploDTO(s.getTipo().name(), s.getNome(), s.getEmail(),
                        s.getCargo() + " · " + s.getOrgao().getNome())));
        return contas;
    }
}
