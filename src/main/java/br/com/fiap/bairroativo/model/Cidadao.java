package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

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
    public boolean podeRegistrarOcorrencias() {
        return true;
    }

    @Override
    public boolean podeAtenderOcorrencias() {
        return false;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }
}
