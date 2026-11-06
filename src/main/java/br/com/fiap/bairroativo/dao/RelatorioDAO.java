package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.dto.EngajamentoDTO;
import br.com.fiap.bairroativo.dto.RelatorioBairroDTO;
import br.com.fiap.bairroativo.dto.RelatorioCategoriaDTO;
import br.com.fiap.bairroativo.dto.RelatorioOrgaoDTO;
import br.com.fiap.bairroativo.dto.ResumoDTO;
import br.com.fiap.bairroativo.exception.DadosException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de relatório com JOIN e agregação (COUNT, SUM, AVG, GROUP BY). */
public class RelatorioDAO {

    private static final String PENDENTE = "o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO')";

    private final ConnectionFactory conexoes;

    public RelatorioDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    public ResumoDTO resumo() {
        String sql = """
                SELECT r.*,
                       (SELECT COUNT(*) FROM BA_APOIO) AS APOIOS,
                       (SELECT COUNT(*) FROM BA_CIDADAO) AS CIDADAOS
                  FROM (SELECT COUNT(*) AS TOTAL,
                               NVL(SUM(CASE WHEN %1$s THEN 1 ELSE 0 END), 0) AS PENDENTES,
                               NVL(SUM(CASE WHEN o.STATUS = 'RESOLVIDA' THEN 1 ELSE 0 END), 0) AS RESOLVIDAS,
                               NVL(SUM(CASE WHEN %1$s AND TRUNC(SYSDATE - o.DT_ABERTURA) > c.PRAZO_DIAS
                                            THEN 1 ELSE 0 END), 0) AS ATRASADAS,
                               ROUND(AVG(o.DT_RESOLUCAO - o.DT_ABERTURA), 1) AS DIAS_MEDIOS
                          FROM BA_OCORRENCIA o
                          JOIN BA_CATEGORIA c ON c.ID_CATEGORIA = o.ID_CATEGORIA) r
                """.formatted(PENDENTE);
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new ResumoDTO(rs.getInt("TOTAL"), rs.getInt("PENDENTES"), rs.getInt("RESOLVIDAS"),
                    rs.getInt("ATRASADAS"), rs.getInt("APOIOS"), rs.getInt("CIDADAOS"),
                    JdbcUtil.doubleOuNull(rs, "DIAS_MEDIOS"));
        } catch (SQLException e) {
            throw DadosException.de("gerar resumo", e);
        }
    }

    public List<RelatorioBairroDTO> porBairro() {
        String sql = """
                SELECT b.ID_BAIRRO, b.NOME, b.ZONA,
                       COUNT(o.ID_OCORRENCIA) AS TOTAL,
                       SUM(CASE WHEN o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO') THEN 1 ELSE 0 END)
                         AS PENDENTES,
                       SUM(CASE WHEN o.STATUS = 'RESOLVIDA' THEN 1 ELSE 0 END) AS RESOLVIDAS,
                       NVL(SUM(ap.QTD), 0) AS APOIOS,
                       ROUND(AVG(o.DT_RESOLUCAO - o.DT_ABERTURA), 1) AS DIAS_MEDIOS
                  FROM BA_BAIRRO b
                  LEFT JOIN BA_OCORRENCIA o ON o.ID_BAIRRO = b.ID_BAIRRO
                  LEFT JOIN (SELECT ID_OCORRENCIA, COUNT(*) AS QTD FROM BA_APOIO GROUP BY ID_OCORRENCIA) ap
                         ON ap.ID_OCORRENCIA = o.ID_OCORRENCIA
                 GROUP BY b.ID_BAIRRO, b.NOME, b.ZONA
                 ORDER BY PENDENTES DESC, TOTAL DESC, b.NOME
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RelatorioBairroDTO> linhas = new ArrayList<>();
            while (rs.next()) {
                linhas.add(new RelatorioBairroDTO(rs.getLong("ID_BAIRRO"), rs.getString("NOME"), rs.getString("ZONA"),
                        rs.getInt("TOTAL"), rs.getInt("PENDENTES"), rs.getInt("RESOLVIDAS"), rs.getInt("APOIOS"),
                        JdbcUtil.doubleOuNull(rs, "DIAS_MEDIOS")));
            }
            return linhas;
        } catch (SQLException e) {
            throw DadosException.de("gerar relatório por bairro", e);
        }
    }

    public List<RelatorioCategoriaDTO> porCategoria() {
        String sql = """
                SELECT c.ID_CATEGORIA, c.NOME, g.SIGLA, c.PRAZO_DIAS,
                       COUNT(o.ID_OCORRENCIA) AS TOTAL,
                       SUM(CASE WHEN o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO') THEN 1 ELSE 0 END)
                         AS PENDENTES,
                       SUM(CASE WHEN o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO')
                                 AND TRUNC(SYSDATE - o.DT_ABERTURA) > c.PRAZO_DIAS THEN 1 ELSE 0 END) AS ATRASADAS,
                       SUM(CASE WHEN o.STATUS = 'RESOLVIDA'
                                 AND o.DT_RESOLUCAO - o.DT_ABERTURA <= c.PRAZO_DIAS THEN 1 ELSE 0 END)
                         AS NO_PRAZO,
                       SUM(CASE WHEN o.STATUS = 'RESOLVIDA' THEN 1 ELSE 0 END) AS RESOLVIDAS
                  FROM BA_CATEGORIA c
                  JOIN BA_ORGAO g ON g.ID_ORGAO = c.ID_ORGAO
                  LEFT JOIN BA_OCORRENCIA o ON o.ID_CATEGORIA = c.ID_CATEGORIA
                 GROUP BY c.ID_CATEGORIA, c.NOME, g.SIGLA, c.PRAZO_DIAS
                 ORDER BY TOTAL DESC, c.NOME
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RelatorioCategoriaDTO> linhas = new ArrayList<>();
            while (rs.next()) {
                linhas.add(new RelatorioCategoriaDTO(rs.getLong("ID_CATEGORIA"), rs.getString("NOME"),
                        rs.getString("SIGLA"), rs.getInt("PRAZO_DIAS"), rs.getInt("TOTAL"), rs.getInt("PENDENTES"),
                        rs.getInt("ATRASADAS"), rs.getInt("NO_PRAZO"), rs.getInt("RESOLVIDAS")));
            }
            return linhas;
        } catch (SQLException e) {
            throw DadosException.de("gerar relatório por categoria", e);
        }
    }

    public List<EngajamentoDTO> engajamento(int limite) {
        String sql = """
                SELECT * FROM (
                    SELECT u.ID_CIDADAO, u.NOME, b.NOME AS BAIRRO,
                           (SELECT COUNT(*) FROM BA_OCORRENCIA o WHERE o.ID_CIDADAO = u.ID_CIDADAO) AS REGISTRADAS,
                           COUNT(a.ID_OCORRENCIA) AS APOIOS
                      FROM BA_CIDADAO u
                      JOIN BA_BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO
                      LEFT JOIN BA_APOIO a ON a.ID_CIDADAO = u.ID_CIDADAO
                     GROUP BY u.ID_CIDADAO, u.NOME, b.NOME)
                 ORDER BY REGISTRADAS + APOIOS DESC, NOME
                 FETCH FIRST ? ROWS ONLY
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<EngajamentoDTO> linhas = new ArrayList<>();
                while (rs.next()) {
                    linhas.add(new EngajamentoDTO(rs.getLong("ID_CIDADAO"), rs.getString("NOME"),
                            rs.getString("BAIRRO"), rs.getInt("REGISTRADAS"), rs.getInt("APOIOS")));
                }
                return linhas;
            }
        } catch (SQLException e) {
            throw DadosException.de("gerar relatório de engajamento", e);
        }
    }

    /** Fila de cada órgão: pendências, atrasos e tempo médio de resolução. */
    public List<RelatorioOrgaoDTO> porOrgao() {
        String sql = """
                SELECT g.ID_ORGAO, g.NOME, g.SIGLA,
                       NVL(SUM(CASE WHEN o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO') THEN 1 ELSE 0 END), 0)
                         AS PENDENTES,
                       NVL(SUM(CASE WHEN o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO')
                                     AND TRUNC(SYSDATE - o.DT_ABERTURA) > c.PRAZO_DIAS THEN 1 ELSE 0 END), 0)
                         AS ATRASADAS,
                       NVL(SUM(CASE WHEN o.STATUS = 'RESOLVIDA' THEN 1 ELSE 0 END), 0) AS RESOLVIDAS,
                       ROUND(AVG(o.DT_RESOLUCAO - o.DT_ABERTURA), 1) AS DIAS_MEDIOS
                  FROM BA_ORGAO g
                  LEFT JOIN BA_CATEGORIA c ON c.ID_ORGAO = g.ID_ORGAO
                  LEFT JOIN BA_OCORRENCIA o ON o.ID_CATEGORIA = c.ID_CATEGORIA
                 GROUP BY g.ID_ORGAO, g.NOME, g.SIGLA
                 ORDER BY PENDENTES DESC, g.NOME
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RelatorioOrgaoDTO> linhas = new ArrayList<>();
            while (rs.next()) {
                linhas.add(new RelatorioOrgaoDTO(rs.getLong("ID_ORGAO"), rs.getString("NOME"), rs.getString("SIGLA"),
                        rs.getInt("PENDENTES"), rs.getInt("ATRASADAS"), rs.getInt("RESOLVIDAS"),
                        JdbcUtil.doubleOuNull(rs, "DIAS_MEDIOS")));
            }
            return linhas;
        } catch (SQLException e) {
            throw DadosException.de("gerar relatório por órgão", e);
        }
    }

    /** Usado pelo endpoint de status para conferir se o banco responde. */
    public boolean bancoDisponivel() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
