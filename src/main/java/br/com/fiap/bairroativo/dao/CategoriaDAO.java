package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.model.Orgao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO implements CrudDAO<Categoria> {

    private static final String SELECT = """
            SELECT c.ID_CATEGORIA, c.NOME, c.DESCRICAO, c.PRAZO_DIAS,
                   g.ID_ORGAO, g.NOME AS NOME_ORGAO, g.SIGLA, g.TELEFONE
              FROM BA_CATEGORIA c
              JOIN BA_ORGAO g ON g.ID_ORGAO = c.ID_ORGAO
            """;

    private final ConnectionFactory conexoes;

    public CategoriaDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Categoria inserir(Categoria categoria) {
        String sql = "INSERT INTO BA_CATEGORIA (NOME, DESCRICAO, PRAZO_DIAS, ID_ORGAO) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_CATEGORIA"})) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setInt(3, categoria.getPrazoDias());
            ps.setLong(4, categoria.getOrgao().getId());
            ps.executeUpdate();
            return buscarPorId(JdbcUtil.idGerado(ps)).orElseThrow();
        } catch (SQLException e) {
            throw DadosException.de("cadastrar categoria", e);
        }
    }

    @Override
    public List<Categoria> listar() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY c.NOME");
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw DadosException.de("listar categorias", e);
        }
    }

    @Override
    public Optional<Categoria> buscarPorId(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE c.ID_CATEGORIA = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar categoria", e);
        }
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE BA_CATEGORIA SET NOME = ?, DESCRICAO = ?, PRAZO_DIAS = ?, ID_ORGAO = ? WHERE ID_CATEGORIA = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setInt(3, categoria.getPrazoDias());
            ps.setLong(4, categoria.getOrgao().getId());
            ps.setLong(5, categoria.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar categoria", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_CATEGORIA WHERE ID_CATEGORIA = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir categoria", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Orgao orgao = new Orgao(rs.getLong("ID_ORGAO"), rs.getString("NOME_ORGAO"), rs.getString("SIGLA"),
                rs.getString("TELEFONE"));
        return new Categoria(rs.getLong("ID_CATEGORIA"), rs.getString("NOME"), rs.getString("DESCRICAO"),
                rs.getInt("PRAZO_DIAS"), orgao);
    }
}
