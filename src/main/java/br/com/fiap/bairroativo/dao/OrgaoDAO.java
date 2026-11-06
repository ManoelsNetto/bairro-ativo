package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Orgao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrgaoDAO implements CrudDAO<Orgao> {

    private static final String SELECT = "SELECT ID_ORGAO, NOME, SIGLA, TELEFONE FROM BA_ORGAO";

    private final ConnectionFactory conexoes;

    public OrgaoDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Orgao inserir(Orgao orgao) {
        String sql = "INSERT INTO BA_ORGAO (NOME, SIGLA, TELEFONE) VALUES (?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_ORGAO"})) {
            ps.setString(1, orgao.getNome());
            ps.setString(2, orgao.getSigla());
            ps.setString(3, orgao.getTelefone());
            ps.executeUpdate();
            orgao.setId(JdbcUtil.idGerado(ps));
            return orgao;
        } catch (SQLException e) {
            throw DadosException.de("cadastrar órgão", e);
        }
    }

    @Override
    public List<Orgao> listar() {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY NOME");
             ResultSet rs = ps.executeQuery()) {
            List<Orgao> orgaos = new ArrayList<>();
            while (rs.next()) {
                orgaos.add(mapear(rs));
            }
            return orgaos;
        } catch (SQLException e) {
            throw DadosException.de("listar órgãos", e);
        }
    }

    @Override
    public Optional<Orgao> buscarPorId(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE ID_ORGAO = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar órgão", e);
        }
    }

    @Override
    public boolean atualizar(Orgao orgao) {
        String sql = "UPDATE BA_ORGAO SET NOME = ?, SIGLA = ?, TELEFONE = ? WHERE ID_ORGAO = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orgao.getNome());
            ps.setString(2, orgao.getSigla());
            ps.setString(3, orgao.getTelefone());
            ps.setLong(4, orgao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar órgão", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_ORGAO WHERE ID_ORGAO = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir órgão", e);
        }
    }

    private Orgao mapear(ResultSet rs) throws SQLException {
        return new Orgao(rs.getLong("ID_ORGAO"), rs.getString("NOME"), rs.getString("SIGLA"),
                rs.getString("TELEFONE"));
    }
}
