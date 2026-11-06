package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.BairroDAO;
import br.com.fiap.bairroativo.dao.CidadaoDAO;
import br.com.fiap.bairroativo.dto.CidadaoRequest;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Cidadao;

import java.util.List;

public class CidadaoService {

    private final CidadaoDAO dao;
    private final BairroDAO bairroDAO;

    public CidadaoService(CidadaoDAO dao, BairroDAO bairroDAO) {
        this.dao = dao;
        this.bairroDAO = bairroDAO;
    }

    public List<Cidadao> listar() {
        return dao.listar();
    }

    public Cidadao buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Cidadão não encontrado."));
    }

    public Cidadao cadastrar(CidadaoRequest req) {
        return dao.inserir(montar(req));
    }

    public Cidadao atualizar(long id, CidadaoRequest req) {
        Cidadao atual = buscar(id);
        Cidadao novo = montar(req);
        novo.setId(id);
        novo.setDataCadastro(atual.getDataCadastro());
        dao.atualizar(novo);
        return buscar(id);
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Cidadão não encontrado.");
        }
    }

    private Cidadao montar(CidadaoRequest req) {
        Validador.corpo(req);
        String nome = Validador.texto(req.nome(), "o nome", 3, 100);
        String email = Validador.email(req.email());
        String telefone = Validador.telefoneOpcional(req.telefone());
        Bairro bairro = bairroDAO.buscarPorId(Validador.id(req.idBairro(), "o bairro onde mora"))
                .orElseThrow(() -> new ValidacaoException("O bairro selecionado não existe."));
        return new Cidadao(null, nome, email, telefone, null, bairro);
    }
}
