package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.exception.RegraNegocioException;
import br.com.fiap.bairroativo.model.AgentePublico;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.StatusOcorrencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Histórico de atendimento (BA_ATUALIZACAO). */
public class AtualizacaoDAO {

    private final ConnectionFactory conexoes;

    public AtualizacaoDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    /**
     * Muda o status da ocorrência e grava o histórico na mesma transação: ou as duas coisas acontecem, ou nenhuma.
     * O UPDATE confere o status anterior para não sobrescrever uma mudança feita por outro agente ao mesmo tempo.
     */
    public Atualizacao registrar(Atualizacao atualizacao, LocalDateTime dataResolucao) {
        String sqlStatus = "UPDATE BA_OCORRENCIA SET STATUS = ?, DT_RESOLUCAO = ? "
                + "WHERE ID_OCORRENCIA = ? AND STATUS = ?";
        String sqlHistorico = "INSERT INTO BA_ATUALIZACAO (ID_OCORRENCIA, ID_AGENTE, STATUS_ANTERIOR, STATUS_NOVO, "
                + "COMENTARIO) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement status = conn.prepareStatement(sqlStatus);
                 PreparedStatement historico = conn.prepareStatement(sqlHistorico, new String[]{"ID_ATUALIZACAO"})) {
                status.setString(1, atualizacao.getStatusNovo().name());
                JdbcUtil.setDataHora(status, 2, dataResolucao);
                status.setLong(3, atualizacao.getIdOcorrencia());
                status.setString(4, atualizacao.getStatusAnterior().name());
                if (status.executeUpdate() == 0) {
                    conn.rollback();
                    throw new RegraNegocioException(
                            "O status desta ocorrência foi alterado por outra pessoa. Recarregue a página.");
                }
                historico.setLong(1, atualizacao.getIdOcorrencia());
                historico.setLong(2, atualizacao.getAgente().getId());
                historico.setString(3, atualizacao.getStatusAnterior().name());
                historico.setString(4, atualizacao.getStatusNovo().name());
                historico.setString(5, atualizacao.getComentario());
                historico.executeUpdate();
                atualizacao.setId(JdbcUtil.idGerado(historico));
                conn.commit();
                atualizacao.setData(LocalDateTime.now());
                return atualizacao;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DadosException.de("atualizar o status da ocorrência", e);
        }
    }

    public List<Atualizacao> listarPorOcorrencia(long idOcorrencia) {
        String sql = """
                SELECT t.ID_ATUALIZACAO, t.ID_OCORRENCIA, t.STATUS_ANTERIOR, t.STATUS_NOVO, t.COMENTARIO,
                       t.DT_ATUALIZACAO, u.ID_USUARIO, u.NOME, u.ORGAO
                  FROM BA_ATUALIZACAO t
                  JOIN BA_USUARIO u ON u.ID_USUARIO = t.ID_AGENTE
                 WHERE t.ID_OCORRENCIA = ?
                 ORDER BY t.DT_ATUALIZACAO, t.ID_ATUALIZACAO
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                List<Atualizacao> lista = new ArrayList<>();
                while (rs.next()) {
                    Atualizacao a = new Atualizacao();
                    a.setId(rs.getLong("ID_ATUALIZACAO"));
                    a.setIdOcorrencia(rs.getLong("ID_OCORRENCIA"));
                    a.setStatusAnterior(StatusOcorrencia.valueOf(rs.getString("STATUS_ANTERIOR")));
                    a.setStatusNovo(StatusOcorrencia.valueOf(rs.getString("STATUS_NOVO")));
                    a.setComentario(rs.getString("COMENTARIO"));
                    a.setData(JdbcUtil.dataHora(rs, "DT_ATUALIZACAO"));
                    a.setAgente(new AgentePublico(rs.getLong("ID_USUARIO"), rs.getString("NOME"), null, null, null,
                            rs.getString("ORGAO"), null));
                    lista.add(a);
                }
                return lista;
            }
        } catch (SQLException e) {
            throw DadosException.de("listar histórico", e);
        }
    }
}
