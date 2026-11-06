package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

/** Pessoa cadastrada na plataforma. Cada subclasse define o que o perfil pode fazer. */
public abstract class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private LocalDateTime dataCadastro;

    protected Usuario() {
    }

    protected Usuario(Long id, String nome, String email, String telefone, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
    }

    public abstract TipoUsuario getTipo();

    /** Cidadãos registram e apoiam ocorrências. */
    public abstract boolean podeRegistrarOcorrencias();

    /** Agentes públicos mudam o status das ocorrências. */
    public abstract boolean podeAtenderOcorrencias();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
