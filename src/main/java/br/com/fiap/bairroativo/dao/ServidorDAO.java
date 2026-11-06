package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Orgao;
import br.com.fiap.bairroativo.model.ServidorPublico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServidorDAO implements CrudDAO<ServidorPublico> {

    private static final String SELECT = """
            SELECT s.ID_SERVIDOR, s.NOME, s.EMAIL, s.TELEFONE, s.MATRICULA, s.CARGO, s.DT_CADASTRO,
                   g.ID_ORGAO, g.NOME AS NOME_ORGAO, g.SIGLA, g.TELEFONE AS TELEFONE_ORGAO
              FROM BA_SERVIDOR s
              JOIN BA_ORGAO g ON g.ID_ORGAO = s.ID_ORGAO
            """;

    private final ConnectionFactory conexoes;

    public ServidorDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public ServidorPublico inserir(ServidorPublico servidor) {
        String sql = "INSERT INTO BA_SERVIDOR (NOME, EMAIL, TELEFONE, MATRICULA, CARGO, ID_ORGAO) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_SERVIDOR"})) {
            preencher(ps, servidor);
            ps.executeUpdate();
            return buscarPorId(JdbcUtil.idGerado(ps)).orElseThrow();
        } catch (SQLException e) {
            throw DadosException.de("cadastrar servidor", e);
        }
    }

    @Override
    public List<ServidorPublico> listar() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY s.NOME");
             ResultSet rs = ps.executeQuery()) {
            List<ServidorPublico> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw DadosException.de("listar servidores", e);
        }
    }

    @Override
    public Optional<ServidorPublico> buscarPorId(long id) {
        return buscarUm(SELECT + " WHERE s.ID_SERVIDOR = ?", id);
    }

    public Optional<ServidorPublico> buscarPorEmail(String email) {
        return buscarUm(SELECT + " WHERE LOWER(s.EMAIL) = LOWER(?)", email);
    }

    @Override
    public boolean atualizar(ServidorPublico servidor) {
        String sql = "UPDATE BA_SERVIDOR SET NOME = ?, EMAIL = ?, TELEFONE = ?, MATRICULA = ?, CARGO = ?, "
                + "ID_ORGAO = ? WHERE ID_SERVIDOR = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, servidor);
            ps.setLong(7, servidor.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar servidor", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_SERVIDOR WHERE ID_SERVIDOR = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir servidor", e);
        }
    }

    private void preencher(PreparedStatement ps, ServidorPublico servidor) throws SQLException {
        ps.setString(1, servidor.getNome());
        ps.setString(2, servidor.getEmail());
        ps.setString(3, servidor.getTelefone());
        ps.setString(4, servidor.getMatricula());
        ps.setString(5, servidor.getCargo());
        ps.setLong(6, servidor.getOrgao().getId());
    }

    private Optional<ServidorPublico> buscarUm(String sql, Object parametro) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar servidor", e);
        }
    }

    private ServidorPublico mapear(ResultSet rs) throws SQLException {
        Orgao orgao = new Orgao(rs.getLong("ID_ORGAO"), rs.getString("NOME_ORGAO"), rs.getString("SIGLA"),
                rs.getString("TELEFONE_ORGAO"));
        return new ServidorPublico(rs.getLong("ID_SERVIDOR"), rs.getString("NOME"), rs.getString("EMAIL"),
                rs.getString("TELEFONE"), JdbcUtil.dataHora(rs, "DT_CADASTRO"), rs.getString("MATRICULA"),
                rs.getString("CARGO"), orgao);
    }
}
