package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.BairroDAO;
import br.com.fiap.bairroativo.dao.UsuarioDAO;
import br.com.fiap.bairroativo.dto.UsuarioRequest;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.RegraNegocioException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.AgentePublico;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.TipoUsuario;
import br.com.fiap.bairroativo.model.Usuario;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO dao;
    private final BairroDAO bairroDAO;

    public UsuarioService(UsuarioDAO dao, BairroDAO bairroDAO) {
        this.dao = dao;
        this.bairroDAO = bairroDAO;
    }

    public List<Usuario> listar(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return dao.listar();
        }
        return dao.listarPorTipo(Validador.opcao(tipo, TipoUsuario.class, "o tipo de usuário"));
    }

    public Usuario buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }

    /** Garante que o id é de um cidadão (só cidadãos registram e apoiam ocorrências). */
    public Cidadao buscarCidadao(long id) {
        Usuario usuario = buscar(id);
        if (!usuario.podeRegistrarOcorrencias()) {
            throw new RegraNegocioException("Somente cidadãos podem registrar ou apoiar ocorrências.");
        }
        return (Cidadao) usuario;
    }

    /** Garante que o id é de um agente público (só agentes mudam o status). */
    public AgentePublico buscarAgente(long id) {
        Usuario usuario = buscar(id);
        if (!usuario.podeAtenderOcorrencias()) {
            throw new RegraNegocioException("Somente agentes públicos podem atualizar o status de uma ocorrência.");
        }
        return (AgentePublico) usuario;
    }

    public Usuario criar(UsuarioRequest req) {
        return dao.inserir(montar(req));
    }

    public Usuario atualizar(long id, UsuarioRequest req) {
        Usuario atual = buscar(id);
        Usuario novo = montar(req);
        if (novo.getTipo() != atual.getTipo()) {
            throw new RegraNegocioException("Não é possível trocar o tipo de um usuário já cadastrado.");
        }
        novo.setId(id);
        novo.setDataCadastro(atual.getDataCadastro());
        dao.atualizar(novo);
        return buscar(id);
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Usuário não encontrado.");
        }
    }

    /** Valida os campos e cria a subclasse certa conforme o tipo informado. */
    Usuario montar(UsuarioRequest req) {
        Validador.corpo(req);
        TipoUsuario tipo = Validador.opcao(req.tipo(), TipoUsuario.class, "o tipo de usuário");
        String nome = Validador.texto(req.nome(), "o nome", 3, 100);
        String email = Validador.email(req.email());
        String telefone = Validador.telefoneOpcional(req.telefone());

        if (tipo == TipoUsuario.CIDADAO) {
            long idBairro = Validador.id(req.idBairro(), "o bairro onde mora");
            Bairro bairro = bairroDAO.buscarPorId(idBairro)
                    .orElseThrow(() -> new ValidacaoException("O bairro selecionado não existe."));
            return new Cidadao(null, nome, email, telefone, null, bairro);
        }
        String orgao = Validador.texto(req.orgao(), "o órgão", 3, 80);
        String matricula = Validador.texto(req.matricula(), "a matrícula", 3, 20).toUpperCase();
        return new AgentePublico(null, nome, email, telefone, null, orgao, matricula);
    }
}
