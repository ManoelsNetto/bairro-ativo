package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.Zona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CidadaoDAO implements CrudDAO<Cidadao> {

    private static final String SELECT = """
            SELECT u.ID_CIDADAO, u.NOME, u.EMAIL, u.TELEFONE, u.DT_CADASTRO,
                   b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, b.ZONA, b.CIDADE
              FROM BA_CIDADAO u
              JOIN BA_BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO
            """;

    private final ConnectionFactory conexoes;

    public CidadaoDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Cidadao inserir(Cidadao cidadao) {
        String sql = "INSERT INTO BA_CIDADAO (NOME, EMAIL, TELEFONE, ID_BAIRRO) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_CIDADAO"})) {
            ps.setString(1, cidadao.getNome());
            ps.setString(2, cidadao.getEmail());
            ps.setString(3, cidadao.getTelefone());
            ps.setLong(4, cidadao.getBairro().getId());
            ps.executeUpdate();
            return buscarPorId(JdbcUtil.idGerado(ps)).orElseThrow();
        } catch (SQLException e) {
            throw DadosException.de("cadastrar cidadão", e);
        }
    }

    @Override
    public List<Cidadao> listar() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY u.NOME");
             ResultSet rs = ps.executeQuery()) {
            List<Cidadao> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw DadosException.de("listar cidadãos", e);
        }
    }

    @Override
    public Optional<Cidadao> buscarPorId(long id) {
        return buscarUm(SELECT + " WHERE u.ID_CIDADAO = ?", id);
    }

    public Optional<Cidadao> buscarPorEmail(String email) {
        return buscarUm(SELECT + " WHERE LOWER(u.EMAIL) = LOWER(?)", email);
    }

    @Override
    public boolean atualizar(Cidadao cidadao) {
        String sql = "UPDATE BA_CIDADAO SET NOME = ?, EMAIL = ?, TELEFONE = ?, ID_BAIRRO = ? WHERE ID_CIDADAO = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cidadao.getNome());
            ps.setString(2, cidadao.getEmail());
            ps.setString(3, cidadao.getTelefone());
            ps.setLong(4, cidadao.getBairro().getId());
            ps.setLong(5, cidadao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar cidadão", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_CIDADAO WHERE ID_CIDADAO = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir cidadão", e);
        }
    }

    private Optional<Cidadao> buscarUm(String sql, Object parametro) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar cidadão", e);
        }
    }

    private Cidadao mapear(ResultSet rs) throws SQLException {
        Bairro bairro = new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NOME_BAIRRO"),
                Zona.valueOf(rs.getString("ZONA")), rs.getString("CIDADE"));
        return new Cidadao(rs.getLong("ID_CIDADAO"), rs.getString("NOME"), rs.getString("EMAIL"),
                rs.getString("TELEFONE"), JdbcUtil.dataHora(rs, "DT_CADASTRO"), bairro);
    }
}
