package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.OrgaoDAO;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.model.Orgao;

import java.util.List;
import java.util.Locale;

public class OrgaoService {

    private final OrgaoDAO dao;

    public OrgaoService(OrgaoDAO dao) {
        this.dao = dao;
    }

    public List<Orgao> listar() {
        return dao.listar();
    }

    public Orgao buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Órgão não encontrado."));
    }

    public Orgao criar(Orgao dados) {
        return dao.inserir(validar(dados));
    }

    public Orgao atualizar(long id, Orgao dados) {
        Orgao orgao = validar(dados);
        orgao.setId(id);
        if (!dao.atualizar(orgao)) {
            throw new NaoEncontradoException("Órgão não encontrado.");
        }
        return orgao;
    }

    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException("Órgão não encontrado.");
        }
    }

    private Orgao validar(Orgao dados) {
        Validador.corpo(dados);
        return new Orgao(null, Validador.texto(dados.getNome(), "o nome do órgão", 3, 80),
                Validador.texto(dados.getSigla(), "a sigla", 2, 10).toUpperCase(Locale.ROOT),
                Validador.telefoneOpcional(dados.getTelefone()));
    }
}
