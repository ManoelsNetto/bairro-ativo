package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

/** Morador que registra e apoia ocorrências. */
public class Cidadao extends Usuario {

    private Bairro bairro;

    public Cidadao() {
    }

    public Cidadao(Long id, String nome, String email, String telefone, LocalDateTime dataCadastro, Bairro bairro) {
        super(id, nome, email, telefone, dataCadastro);
        this.bairro = bairro;
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.CIDADAO;
    }

    @Override
    public String getDescricaoPerfil() {
        return "Cidadão" + (bairro != null && bairro.getNome() != null ? " · " + bairro.getNome() : "");
    }

    /** Só quem registrou edita, e só enquanto ninguém começou a analisar. */
    @Override
    public boolean podeEditar(Ocorrencia ocorrencia) {
        return ocorrencia.getAutor() != null && getId() != null
                && getId().equals(ocorrencia.getAutor().getId())
                && ocorrencia.getStatus() == StatusOcorrencia.ABERTA;
    }

    @Override
    public boolean podeAtender(Ocorrencia ocorrencia) {
        return false;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }
}
