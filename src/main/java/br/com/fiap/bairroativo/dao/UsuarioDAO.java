package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.AgentePublico;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.TipoUsuario;
import br.com.fiap.bairroativo.model.Usuario;
import br.com.fiap.bairroativo.model.Zona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Cidadãos e agentes ficam na mesma tabela; a coluna TIPO decide qual subclasse é criada. */
public class UsuarioDAO implements CrudDAO<Usuario> {

    private static final String SELECT = """
            SELECT u.ID_USUARIO, u.TIPO, u.NOME, u.EMAIL, u.TELEFONE, u.ORGAO, u.MATRICULA, u.DT_CADASTRO,
                   b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, b.ZONA, b.CIDADE
              FROM BA_USUARIO u
              LEFT JOIN BA_BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO
            """;

    private final ConnectionFactory conexoes;

    public UsuarioDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO BA_USUARIO (TIPO, NOME, EMAIL, TELEFONE, ID_BAIRRO, ORGAO, MATRICULA) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_USUARIO"})) {
            ps.setString(1, usuario.getTipo().name());
            preencherCampos(ps, usuario, 2);
            ps.executeUpdate();
            usuario.setId(JdbcUtil.idGerado(ps));
            return buscarPorId(usuario.getId()).orElse(usuario);
        } catch (SQLException e) {
            throw DadosException.de("cadastrar usuário", e);
        }
    }

    @Override
    public List<Usuario> listar() {
        return consultar(SELECT + " ORDER BY u.NOME", null);
    }

    public List<Usuario> listarPorTipo(TipoUsuario tipo) {
        return consultar(SELECT + " WHERE u.TIPO = ? ORDER BY u.NOME", tipo.name());
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE u.ID_USUARIO = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar usuário", e);
        }
    }

    @Override
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE BA_USUARIO SET NOME = ?, EMAIL = ?, TELEFONE = ?, ID_BAIRRO = ?, ORGAO = ?, "
                + "MATRICULA = ? WHERE ID_USUARIO = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencherCampos(ps, usuario, 1);
            ps.setLong(7, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar usuário", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_USUARIO WHERE ID_USUARIO = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir usuário", e);
        }
    }

    /** Preenche NOME, EMAIL, TELEFONE, ID_BAIRRO, ORGAO e MATRICULA a partir da posição informada. */
    private void preencherCampos(PreparedStatement ps, Usuario usuario, int inicio) throws SQLException {
        ps.setString(inicio, usuario.getNome());
        ps.setString(inicio + 1, usuario.getEmail());
        ps.setString(inicio + 2, usuario.getTelefone());
        if (usuario instanceof Cidadao cidadao) {
            JdbcUtil.setLong(ps, inicio + 3, cidadao.getBairro().getId());
            ps.setString(inicio + 4, null);
            ps.setString(inicio + 5, null);
        } else if (usuario instanceof AgentePublico agente) {
            JdbcUtil.setLong(ps, inicio + 3, null);
            ps.setString(inicio + 4, agente.getOrgao());
            ps.setString(inicio + 5, agente.getMatricula());
        }
    }

    private List<Usuario> consultar(String sql, String parametro) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Usuario> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(mapear(rs));
                }
                return usuarios;
            }
        } catch (SQLException e) {
            throw DadosException.de("listar usuários", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("TIPO"));
        if (tipo == TipoUsuario.AGENTE) {
            return new AgentePublico(rs.getLong("ID_USUARIO"), rs.getString("NOME"), rs.getString("EMAIL"),
                    rs.getString("TELEFONE"), JdbcUtil.dataHora(rs, "DT_CADASTRO"),
                    rs.getString("ORGAO"), rs.getString("MATRICULA"));
        }
        Bairro bairro = new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NOME_BAIRRO"),
                Zona.valueOf(rs.getString("ZONA")), rs.getString("CIDADE"));
        return new Cidadao(rs.getLong("ID_USUARIO"), rs.getString("NOME"), rs.getString("EMAIL"),
                rs.getString("TELEFONE"), JdbcUtil.dataHora(rs, "DT_CADASTRO"), bairro);
    }
}
