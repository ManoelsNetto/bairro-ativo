package br.com.fiap.bairroativo.model;

/** Órgão público responsável por atender um conjunto de categorias (ex.: Iluminação Pública). */
public class Orgao {

    private Long id;
    private String nome;
    private String sigla;
    private String telefone;

    public Orgao() {
    }

    public Orgao(Long id, String nome, String sigla, String telefone) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.telefone = telefone;
    }

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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
