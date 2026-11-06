package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.CategoriaDAO;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Categoria;

import java.util.List;

public class CategoriaService {

    private final CategoriaDAO dao;

    public CategoriaService(CategoriaDAO dao) {
        this.dao = dao;
    }

    public List<Categoria> listar() {
        return dao.listar();
    }

    public Categoria buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Categoria não encontrada."));
    }

    public Categoria criar(Categoria dados) {
        return dao.inserir(validar(dados));
    }

    public Categoria atualizar(long id, Categoria dados) {
        Categoria categoria = validar(dados);
        categoria.setId(id);
        if (!dao.atualizar(categoria)) {
            throw new NaoEncontradoException("Categoria não encontrada.");
        }
        return categoria;
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Categoria não encontrada.");
        }
    }

    Categoria validar(Categoria dados) {
        Validador.corpo(dados);
        if (dados.getPrazoDias() < 1 || dados.getPrazoDias() > 365) {
            throw new ValidacaoException("O prazo de atendimento deve ficar entre 1 e 365 dias.");
        }
        return new Categoria(null, Validador.texto(dados.getNome(), "o nome da categoria", 3, 60),
                Validador.textoOpcional(dados.getDescricao(), "a descrição", 200), dados.getPrazoDias());
    }
}
