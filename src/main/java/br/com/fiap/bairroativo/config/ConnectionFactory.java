package br.com.fiap.bairroativo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fornece conexões JDBC com o Oracle usando o DriverManager.
 * Cada DAO abre a conexão com try-with-resources, que a fecha automaticamente ao final.
 * Os dados de acesso vêm do arquivo .env (ou de variáveis de ambiente).
 */
public final class ConnectionFactory {

    private final String url;
    private final String usuario;
    private final String senha;

    public ConnectionFactory() {
        this.url = Config.get("DB_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl");
        this.usuario = Config.obrigatoria("DB_USER");
        this.senha = Config.obrigatoria("DB_PASSWORD");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
