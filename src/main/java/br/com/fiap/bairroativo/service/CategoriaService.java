package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.CategoriaDAO;
import br.com.fiap.bairroativo.dao.OrgaoDAO;
import br.com.fiap.bairroativo.dto.CategoriaRequest;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.model.Orgao;

import java.util.List;

public class CategoriaService {

    private final CategoriaDAO dao;
    private final OrgaoDAO orgaoDAO;

    public CategoriaService(CategoriaDAO dao, OrgaoDAO orgaoDAO) {
        this.dao = dao;
        this.orgaoDAO = orgaoDAO;
    }

    public List<Categoria> listar() {
        return dao.listar();
    }

    public Categoria buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Categoria não encontrada."));
    }

    public Categoria criar(CategoriaRequest req) {
        return dao.inserir(montar(req));
    }

    public Categoria atualizar(long id, CategoriaRequest req) {
        Categoria categoria = montar(req);
        categoria.setId(id);
        if (!dao.atualizar(categoria)) {
            throw new NaoEncontradoException("Categoria não encontrada.");
        }
        return buscar(id);
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Categoria não encontrada.");
        }
    }

    private Categoria montar(CategoriaRequest req) {
        Validador.corpo(req);
        if (req.prazoDias() == null || req.prazoDias() < 1 || req.prazoDias() > 365) {
            throw new ValidacaoException("O prazo de atendimento deve ficar entre 1 e 365 dias.");
        }
        Orgao orgao = orgaoDAO.buscarPorId(Validador.id(req.idOrgao(), "o órgão responsável"))
                .orElseThrow(() -> new ValidacaoException("O órgão selecionado não existe."));
        return new Categoria(null, Validador.texto(req.nome(), "o nome da categoria", 3, 60),
                Validador.textoOpcional(req.descricao(), "a descrição", 200), req.prazoDias(), orgao);
    }
}
