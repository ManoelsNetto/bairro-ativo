package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

/** Servidor da prefeitura. Atende as ocorrências das categorias do seu órgão. */
public class ServidorPublico extends Usuario {

    private String matricula;
    private String cargo;
    private Orgao orgao;

    public ServidorPublico() {
    }

    public ServidorPublico(Long id, String nome, String email, String telefone, LocalDateTime dataCadastro,
                           String matricula, String cargo, Orgao orgao) {
        super(id, nome, email, telefone, dataCadastro);
        this.matricula = matricula;
        this.cargo = cargo;
        this.orgao = orgao;
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.SERVIDOR;
    }

    @Override
    public String getDescricaoPerfil() {
        return "Servidor público" + (orgao != null && orgao.getSigla() != null ? " · " + orgao.getSigla() : "");
    }

    @Override
    public boolean podeEditar(Ocorrencia ocorrencia) {
        return false;
    }

    /** Atende se a ocorrência ainda está pendente e a categoria é do seu órgão. */
    @Override
    public boolean podeAtender(Ocorrencia ocorrencia) {
        return ocorrencia.isPendente() && orgao != null && ocorrencia.getCategoria() != null
                && ocorrencia.getCategoria().getOrgao() != null
                && orgao.getId().equals(ocorrencia.getCategoria().getOrgao().getId());
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Orgao getOrgao() {
        return orgao;
    }

    public void setOrgao(Orgao orgao) {
        this.orgao = orgao;
    }
}
