package br.com.fiap.bairroativo.model;

/** Tipo de problema, com prazo esperado de atendimento e o órgão que cuida dele. */
public class Categoria {

    private Long id;
    private String nome;
    private String descricao;
    private int prazoDias;
    private Orgao orgao;

    public Categoria() {
    }

    public Categoria(Long id, String nome, String descricao, int prazoDias, Orgao orgao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.prazoDias = prazoDias;
        this.orgao = orgao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getPrazoDias() {
        return prazoDias;
    }

    public void setPrazoDias(int prazoDias) {
        this.prazoDias = prazoDias;
    }

    public Orgao getOrgao() {
        return orgao;
    }

    public void setOrgao(Orgao orgao) {
        this.orgao = orgao;
    }
}
