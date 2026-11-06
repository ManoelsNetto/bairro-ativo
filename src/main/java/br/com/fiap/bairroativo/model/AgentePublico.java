package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

public class AgentePublico extends Usuario {

    private String orgao;
    private String matricula;

    public AgentePublico() {
    }

    public AgentePublico(Long id, String nome, String email, String telefone, LocalDateTime dataCadastro,
                         String orgao, String matricula) {
        super(id, nome, email, telefone, dataCadastro);
        this.orgao = orgao;
        this.matricula = matricula;
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.AGENTE;
    }

    @Override
    public boolean podeRegistrarOcorrencias() {
        return false;
    }

    @Override
    public boolean podeAtenderOcorrencias() {
        return true;
    }

    public String getOrgao() {
        return orgao;
    }

    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
