package br.com.fiap.bairroativo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/** Registro de uma mudança de status feita por um servidor público. */
public class Atualizacao {

    private Long id;
    private Long idOcorrencia;
    private ServidorPublico servidor;
    private StatusOcorrencia statusAnterior;
    private StatusOcorrencia statusNovo;
    private String comentario;
    private LocalDateTime data;

    public Atualizacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(Long idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    /** No histórico público aparecem só nome, cargo e órgão do servidor. */
    @JsonIgnoreProperties({"email", "telefone", "matricula", "dataCadastro", "descricaoPerfil"})
    public ServidorPublico getServidor() {
        return servidor;
    }

    public void setServidor(ServidorPublico servidor) {
        this.servidor = servidor;
    }

    public StatusOcorrencia getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusOcorrencia statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusOcorrencia getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusOcorrencia statusNovo) {
        this.statusNovo = statusNovo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
