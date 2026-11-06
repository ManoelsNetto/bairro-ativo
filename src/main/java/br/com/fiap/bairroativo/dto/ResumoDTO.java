package br.com.fiap.bairroativo.dto;

public record ResumoDTO(int total, int pendentes, int resolvidas, int atrasadas, int apoios,
                        int cidadaos, Double diasMediosResolucao) {
}
