package br.com.fiap.bairroativo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/** Associação N:N entre cidadão e ocorrência: "isso também me afeta". */
public class Apoio {

    private Cidadao cidadao;
    private Long idOcorrencia;
    private LocalDateTime data;

    public Apoio() {
    }

    public Apoio(Cidadao cidadao, Long idOcorrencia, LocalDateTime data) {
        this.cidadao = cidadao;
        this.idOcorrencia = idOcorrencia;
        this.data = data;
    }

    @JsonIgnoreProperties({"email", "telefone", "dataCadastro"})
    public Cidadao getCidadao() {
        return cidadao;
    }

    public void setCidadao(Cidadao cidadao) {
        this.cidadao = cidadao;
    }

    public Long getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(Long idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
