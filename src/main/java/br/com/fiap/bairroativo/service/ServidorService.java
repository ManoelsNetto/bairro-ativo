package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.OrgaoDAO;
import br.com.fiap.bairroativo.dao.ServidorDAO;
import br.com.fiap.bairroativo.dto.ServidorRequest;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.RegraNegocioException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Orgao;
import br.com.fiap.bairroativo.model.ServidorPublico;

import java.util.List;
import java.util.Locale;

public class ServidorService {

    private final ServidorDAO dao;
    private final OrgaoDAO orgaoDAO;

    public ServidorService(ServidorDAO dao, OrgaoDAO orgaoDAO) {
        this.dao = dao;
        this.orgaoDAO = orgaoDAO;
    }

    public List<ServidorPublico> listar() {
        return dao.listar();
    }

    public ServidorPublico buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Servidor não encontrado."));
    }

    public ServidorPublico cadastrar(ServidorRequest req) {
        return dao.inserir(montar(req));
    }

    public ServidorPublico atualizar(long id, ServidorRequest req) {
        ServidorPublico atual = buscar(id);
        ServidorPublico novo = montar(req);
        novo.setId(id);
        novo.setDataCadastro(atual.getDataCadastro());
        dao.atualizar(novo);
        return buscar(id);
    }

    /** O servidor conectado não pode excluir o próprio cadastro (perderia o acesso no meio da sessão). */
    public void excluir(long id, ServidorPublico quemExclui) {
        if (quemExclui.getId() == id) {
            throw new RegraNegocioException("Você não pode excluir o próprio cadastro enquanto está conectado.");
        }
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Servidor não encontrado.");
        }
    }

    private ServidorPublico montar(ServidorRequest req) {
        Validador.corpo(req);
        String nome = Validador.texto(req.nome(), "o nome", 3, 100);
        String email = Validador.email(req.email());
        String telefone = Validador.telefoneOpcional(req.telefone());
        String matricula = Validador.texto(req.matricula(), "a matrícula", 3, 20).toUpperCase(Locale.ROOT);
        String cargo = Validador.texto(req.cargo(), "o cargo", 3, 60);
        Orgao orgao = orgaoDAO.buscarPorId(Validador.id(req.idOrgao(), "o órgão"))
                .orElseThrow(() -> new ValidacaoException("O órgão selecionado não existe."));
        return new ServidorPublico(null, nome, email, telefone, null, matricula, cargo, orgao);
    }
}
