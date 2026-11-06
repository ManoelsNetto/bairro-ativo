package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Apoio;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.Zona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Tabela associativa BA_APOIO (N:N entre cidadão e ocorrência). */
public class ApoioDAO {

    private final ConnectionFactory conexoes;

    public ApoioDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    public void inserir(long idCidadao, long idOcorrencia) {
        String sql = "INSERT INTO BA_APOIO (ID_CIDADAO, ID_OCORRENCIA) VALUES (?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCidadao);
            ps.setLong(2, idOcorrencia);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DadosException.de("registrar apoio", e);
        }
    }

    public boolean excluir(long idCidadao, long idOcorrencia) {
        String sql = "DELETE FROM BA_APOIO WHERE ID_CIDADAO = ? AND ID_OCORRENCIA = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCidadao);
            ps.setLong(2, idOcorrencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("remover apoio", e);
        }
    }

    public List<Apoio> listarPorOcorrencia(long idOcorrencia) {
        String sql = """
                SELECT a.ID_OCORRENCIA, a.DT_APOIO, u.ID_CIDADAO, u.NOME,
                       b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, b.ZONA, b.CIDADE
                  FROM BA_APOIO a
                  JOIN BA_CIDADAO u ON u.ID_CIDADAO = a.ID_CIDADAO
                  JOIN BA_BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO
                 WHERE a.ID_OCORRENCIA = ?
                 ORDER BY a.DT_APOIO DESC
                """;
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                List<Apoio> apoios = new ArrayList<>();
                while (rs.next()) {
                    Bairro bairro = new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NOME_BAIRRO"),
                            Zona.valueOf(rs.getString("ZONA")), rs.getString("CIDADE"));
                    Cidadao cidadao = new Cidadao(rs.getLong("ID_CIDADAO"), rs.getString("NOME"), null, null,
                            null, bairro);
                    apoios.add(new Apoio(cidadao, rs.getLong("ID_OCORRENCIA"), JdbcUtil.dataHora(rs, "DT_APOIO")));
                }
                return apoios;
            }
        } catch (SQLException e) {
            throw DadosException.de("listar apoios", e);
        }
    }
}
