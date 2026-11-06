package br.com.fiap.bairroativo.model;

import java.time.LocalDateTime;

/**
 * Pessoa que usa a plataforma. Cidadão e servidor público herdam os dados comuns
 * e cada um define, de forma polimórfica, o que pode fazer com uma ocorrência.
 */
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

    /** Texto curto que aparece no topo da tela, ex.: "Cidadã(o) · Vila Mariana". */
    public abstract String getDescricaoPerfil();

    /** Pode corrigir ou excluir a ocorrência? */
    public abstract boolean podeEditar(Ocorrencia ocorrencia);

    /** Pode mudar o status da ocorrência? */
    public abstract boolean podeAtender(Ocorrencia ocorrencia);

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
