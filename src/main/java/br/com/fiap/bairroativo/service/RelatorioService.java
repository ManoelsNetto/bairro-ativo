package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.RelatorioDAO;
import br.com.fiap.bairroativo.dto.EngajamentoDTO;
import br.com.fiap.bairroativo.dto.RelatorioBairroDTO;
import br.com.fiap.bairroativo.dto.RelatorioCategoriaDTO;
import br.com.fiap.bairroativo.dto.RelatorioOrgaoDTO;
import br.com.fiap.bairroativo.dto.ResumoDTO;
import br.com.fiap.bairroativo.exception.ValidacaoException;

import java.util.List;

public class RelatorioService {

    private final RelatorioDAO dao;

    public RelatorioService(RelatorioDAO dao) {
        this.dao = dao;
    }

    public ResumoDTO resumo() {
        return dao.resumo();
    }

    public List<RelatorioBairroDTO> porBairro() {
        return dao.porBairro();
    }

    public List<RelatorioCategoriaDTO> porCategoria() {
        return dao.porCategoria();
    }

    public List<RelatorioOrgaoDTO> porOrgao() {
        return dao.porOrgao();
    }

    public List<EngajamentoDTO> engajamento(int limite) {
        if (limite < 1 || limite > 50) {
            throw new ValidacaoException("O limite deve ficar entre 1 e 50.");
        }
        return dao.engajamento(limite);
    }

    public boolean bancoDisponivel() {
        return dao.bancoDisponivel();
    }
}
