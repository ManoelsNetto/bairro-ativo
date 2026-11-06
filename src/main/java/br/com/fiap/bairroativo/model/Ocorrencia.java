package br.com.fiap.bairroativo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Problema do espaço público registrado por um cidadão.
 * A prioridade cresce com os apoios da vizinhança, o tempo em aberto e o atraso em relação ao prazo da categoria.
 */
public class Ocorrencia implements Priorizavel {

    public static final int PONTOS_POR_APOIO = 3;
    public static final int PONTOS_ATRASO = 10;

    private Long id;
    private String titulo;
    private String descricao;
    private String endereco;
    private String referencia;
    private Bairro bairro;
    private Categoria categoria;
    private Cidadao autor;
    private StatusOcorrencia status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataResolucao;
    private int totalApoios;

    public Ocorrencia() {
    }

    public long getDiasEmAberto() {
        return diasEmAberto(LocalDateTime.now());
    }

    public long diasEmAberto(LocalDateTime agora) {
        if (dataAbertura == null) {
            return 0;
        }
        LocalDateTime fim = dataResolucao != null ? dataResolucao : agora;
        return Math.max(0, Duration.between(dataAbertura, fim).toDays());
    }

    public boolean isAtrasada() {
        return atrasada(LocalDateTime.now());
    }

    public boolean atrasada(LocalDateTime agora) {
        return isPendente() && categoria != null && diasEmAberto(agora) > categoria.getPrazoDias();
    }

    public boolean isPendente() {
        return status != null && status.isPendente();
    }

    @Override
    public int getPontuacaoPrioridade() {
        return pontuacao(LocalDateTime.now());
    }

    public int pontuacao(LocalDateTime agora) {
        if (!isPendente()) {
            return 0;
        }
        int pontos = totalApoios * PONTOS_POR_APOIO + (int) diasEmAberto(agora);
        return atrasada(agora) ? pontos + PONTOS_ATRASO : pontos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /** Na resposta pública só aparecem id e nome do autor (dados de contato ficam protegidos). */
    @JsonIgnoreProperties({"email", "telefone", "bairro", "dataCadastro"})
    public Cidadao getAutor() {
        return autor;
    }

    public void setAutor(Cidadao autor) {
        this.autor = autor;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public void setStatus(StatusOcorrencia status) {
        this.status = status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public int getTotalApoios() {
        return totalApoios;
    }

    public void setTotalApoios(int totalApoios) {
        this.totalApoios = totalApoios;
    }
}
