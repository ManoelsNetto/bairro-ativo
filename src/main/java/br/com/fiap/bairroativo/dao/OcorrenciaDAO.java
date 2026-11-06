package br.com.fiap.bairroativo.dao;

import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.dto.FiltroOcorrencia;
import br.com.fiap.bairroativo.exception.DadosException;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.Orgao;
import br.com.fiap.bairroativo.model.StatusOcorrencia;
import br.com.fiap.bairroativo.model.Zona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OcorrenciaDAO implements CrudDAO<Ocorrencia> {

    /** Já traz bairro, categoria, autor e a contagem de apoios em uma única consulta. */
    private static final String SELECT = """
            SELECT o.ID_OCORRENCIA, o.TITULO, o.DESCRICAO, o.ENDERECO, o.REFERENCIA, o.STATUS,
                   o.DT_ABERTURA, o.DT_RESOLUCAO,
                   b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, b.ZONA, b.CIDADE,
                   c.ID_CATEGORIA, c.NOME AS NOME_CATEGORIA, c.DESCRICAO AS DESCRICAO_CATEGORIA, c.PRAZO_DIAS,
                   g.ID_ORGAO, g.NOME AS NOME_ORGAO, g.SIGLA, g.TELEFONE AS TELEFONE_ORGAO,
                   u.ID_CIDADAO, u.NOME AS NOME_AUTOR,
                   (SELECT COUNT(*) FROM BA_APOIO a WHERE a.ID_OCORRENCIA = o.ID_OCORRENCIA) AS TOTAL_APOIOS
              FROM BA_OCORRENCIA o
              JOIN BA_BAIRRO b ON b.ID_BAIRRO = o.ID_BAIRRO
              JOIN BA_CATEGORIA c ON c.ID_CATEGORIA = o.ID_CATEGORIA
              JOIN BA_ORGAO g ON g.ID_ORGAO = c.ID_ORGAO
              JOIN BA_CIDADAO u ON u.ID_CIDADAO = o.ID_CIDADAO
            """;

    private final ConnectionFactory conexoes;

    public OcorrenciaDAO(ConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Ocorrencia inserir(Ocorrencia ocorrencia) {
        String sql = "INSERT INTO BA_OCORRENCIA (TITULO, DESCRICAO, ENDERECO, REFERENCIA, ID_BAIRRO, ID_CATEGORIA, "
                + "ID_CIDADAO, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_OCORRENCIA"})) {
            ps.setString(1, ocorrencia.getTitulo());
            ps.setString(2, ocorrencia.getDescricao());
            ps.setString(3, ocorrencia.getEndereco());
            ps.setString(4, ocorrencia.getReferencia());
            ps.setLong(5, ocorrencia.getBairro().getId());
            ps.setLong(6, ocorrencia.getCategoria().getId());
            ps.setLong(7, ocorrencia.getAutor().getId());
            ps.setString(8, ocorrencia.getStatus().name());
            ps.executeUpdate();
            long id = JdbcUtil.idGerado(ps);
            return buscarPorId(id).orElseThrow();
        } catch (SQLException e) {
            throw DadosException.de("registrar ocorrência", e);
        }
    }

    @Override
    public List<Ocorrencia> listar() {
        return listar(FiltroOcorrencia.simples(null, null, null, null));
    }

    /** Listagem com filtros opcionais; a ordem padrão é das mais recentes para as mais antigas. */
    public List<Ocorrencia> listar(FiltroOcorrencia filtro) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (filtro.status() != null) {
            sql.append(" AND o.STATUS = ?");
            parametros.add(filtro.status().name());
        }
        if (filtro.somentePendentes()) {
            sql.append(" AND o.STATUS IN ('ABERTA', 'EM_ANALISE', 'EM_ANDAMENTO')");
        }
        if (filtro.idBairro() != null) {
            sql.append(" AND o.ID_BAIRRO = ?");
            parametros.add(filtro.idBairro());
        }
        if (filtro.idCategoria() != null) {
            sql.append(" AND o.ID_CATEGORIA = ?");
            parametros.add(filtro.idCategoria());
        }
        if (filtro.idAutor() != null) {
            sql.append(" AND o.ID_CIDADAO = ?");
            parametros.add(filtro.idAutor());
        }
        if (filtro.idApoiador() != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM BA_APOIO a WHERE a.ID_OCORRENCIA = o.ID_OCORRENCIA"
                    + " AND a.ID_CIDADAO = ?)");
            parametros.add(filtro.idApoiador());
        }
        if (filtro.idOrgao() != null) {
            sql.append(" AND c.ID_ORGAO = ?");
            parametros.add(filtro.idOrgao());
        }
        if (filtro.texto() != null && !filtro.texto().isBlank()) {
            sql.append(" AND (UPPER(o.TITULO) LIKE ? OR UPPER(o.ENDERECO) LIKE ? OR UPPER(o.DESCRICAO) LIKE ?)");
            String termo = "%" + filtro.texto().trim().toUpperCase() + "%";
            parametros.add(termo);
            parametros.add(termo);
            parametros.add(termo);
        }
        sql.append(" ORDER BY o.DT_ABERTURA DESC");

        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Ocorrencia> ocorrencias = new ArrayList<>();
                while (rs.next()) {
                    ocorrencias.add(mapear(rs));
                }
                return ocorrencias;
            }
        } catch (SQLException e) {
            throw DadosException.de("listar ocorrências", e);
        }
    }

    @Override
    public Optional<Ocorrencia> buscarPorId(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE o.ID_OCORRENCIA = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DadosException.de("buscar ocorrência", e);
        }
    }

    /** Atualiza os dados descritivos. Status e autor só mudam pelo fluxo de atendimento. */
    @Override
    public boolean atualizar(Ocorrencia ocorrencia) {
        String sql = "UPDATE BA_OCORRENCIA SET TITULO = ?, DESCRICAO = ?, ENDERECO = ?, REFERENCIA = ?, "
                + "ID_BAIRRO = ?, ID_CATEGORIA = ? WHERE ID_OCORRENCIA = ?";
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ocorrencia.getTitulo());
            ps.setString(2, ocorrencia.getDescricao());
            ps.setString(3, ocorrencia.getEndereco());
            ps.setString(4, ocorrencia.getReferencia());
            ps.setLong(5, ocorrencia.getBairro().getId());
            ps.setLong(6, ocorrencia.getCategoria().getId());
            ps.setLong(7, ocorrencia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("atualizar ocorrência", e);
        }
    }

    /** Apoios e histórico são apagados junto (ON DELETE CASCADE). */
    @Override
    public boolean excluir(long id) {
        try (Connection conn = conexoes.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BA_OCORRENCIA WHERE ID_OCORRENCIA = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DadosException.de("excluir ocorrência", e);
        }
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("ID_OCORRENCIA"));
        o.setTitulo(rs.getString("TITULO"));
        o.setDescricao(rs.getString("DESCRICAO"));
        o.setEndereco(rs.getString("ENDERECO"));
        o.setReferencia(rs.getString("REFERENCIA"));
        o.setStatus(StatusOcorrencia.valueOf(rs.getString("STATUS")));
        o.setDataAbertura(JdbcUtil.dataHora(rs, "DT_ABERTURA"));
        o.setDataResolucao(JdbcUtil.dataHora(rs, "DT_RESOLUCAO"));
        o.setBairro(new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NOME_BAIRRO"),
                Zona.valueOf(rs.getString("ZONA")), rs.getString("CIDADE")));
        Orgao orgao = new Orgao(rs.getLong("ID_ORGAO"), rs.getString("NOME_ORGAO"), rs.getString("SIGLA"),
                rs.getString("TELEFONE_ORGAO"));
        o.setCategoria(new Categoria(rs.getLong("ID_CATEGORIA"), rs.getString("NOME_CATEGORIA"),
                rs.getString("DESCRICAO_CATEGORIA"), rs.getInt("PRAZO_DIAS"), orgao));
        Cidadao autor = new Cidadao();
        autor.setId(rs.getLong("ID_CIDADAO"));
        autor.setNome(rs.getString("NOME_AUTOR"));
        o.setAutor(autor);
        o.setTotalApoios(rs.getInt("TOTAL_APOIOS"));
        return o;
    }
}
