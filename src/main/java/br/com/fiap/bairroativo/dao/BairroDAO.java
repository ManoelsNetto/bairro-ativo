package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Zona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BairroDAO implements CrudDAO<Bairro> {

    private static final String SELECT = "SELECT ID_BAIRRO, NOME, ZONA, CIDADE FROM BA_BAIRRO";

    private final ConnectionFactory conexoes;

    public BairroDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Bairro inserir(Bairro bairro) {
        String sql = "INSERT INTO BA_BAIRRO (NOME, ZONA, CIDADE) VALUES (?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_BAIRRO"})) {
            ps.setString(1, bairro.getNome());
            ps.setString(2, bairro.getZona().name());
            ps.setString(3, bairro.getCidade());
            ps.executeUpdate();
            bairro.setId(JdbcUtil.idGerado(ps));
            return bairro;
        } catch (SQLException e) {
            throw DadosException.de("cadastrar bairro", e);
        }
    }

    @Override
    public List<Bairro> listar() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY NOME");
             ResultSet rs = ps.executeQuery()) {
            List<Bairro> bairros = new ArrayList<>();
            while (rs.next()) {
                bairros.add(mapear(rs));
            }
            return bairros;
        } catch (SQLException e) {
            throw DadosException.de("listar bairros", e);
        }
    }

    @Override
    public Optional<Bairro> buscarPorId(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE ID_BAIRRO = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar bairro", e);
        }
    }

    @Override
    public boolean atualizar(Bairro bairro) {
        String sql = "UPDATE BA_BAIRRO SET NOME = ?, ZONA = ?, CIDADE = ? WHERE ID_BAIRRO = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bairro.getNome());
            ps.setString(2, bairro.getZona().name());
            ps.setString(3, bairro.getCidade());
            ps.setLong(4, bairro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar bairro", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_BAIRRO WHERE ID_BAIRRO = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir bairro", e);
        }
    }

    private Bairro mapear(ResultSet rs) throws SQLException {
        return new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NOME"), Zona.valueOf(rs.getString("ZONA")),
                rs.getString("CIDADE"));
    }
}
