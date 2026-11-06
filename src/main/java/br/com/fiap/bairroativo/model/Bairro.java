package br.com.fiap.bairroativo.model;

public class Bairro {

    private Long id;
    private String nome;
    private Zona zona;
    private String cidade;

    public Bairro() {
    }

    public Bairro(Long id, String nome, Zona zona, String cidade) {
        this.id = id;
        this.nome = nome;
        this.zona = zona;
        this.cidade = cidade;
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

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
