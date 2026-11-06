package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.BairroDAO;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Bairro;

import java.util.List;

public class BairroService {

    private final BairroDAO dao;

    public BairroService(BairroDAO dao) {
        this.dao = dao;
    }

    public List<Bairro> listar() {
        return dao.listar();
    }

    public Bairro buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Bairro não encontrado."));
    }

    public Bairro criar(Bairro dados) {
        return dao.inserir(validar(dados));
    }

    public Bairro atualizar(long id, Bairro dados) {
        Bairro bairro = validar(dados);
        bairro.setId(id);
        if (!dao.atualizar(bairro)) {
            throw new NaoEncontradoException("Bairro não encontrado.");
        }
        return bairro;
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Bairro não encontrado.");
        }
    }

    Bairro validar(Bairro dados) {
        Validador.corpo(dados);
        if (dados.getZona() == null) {
            throw new ValidacaoException("Selecione a zona.");
        }
        String cidade = dados.getCidade() == null || dados.getCidade().isBlank()
                ? "São Paulo" : Validador.texto(dados.getCidade(), "a cidade", 2, 60);
        return new Bairro(null, Validador.texto(dados.getNome(), "o nome do bairro", 2, 80),
                dados.getZona(), cidade);
    }
}
