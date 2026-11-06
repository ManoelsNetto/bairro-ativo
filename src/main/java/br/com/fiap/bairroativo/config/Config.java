package br.com.fiap.bairroativo.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Lê variáveis de ambiente ou, em desenvolvimento, o arquivo .env. */
public final class Config {

    private static final Properties ARQUIVO_ENV = carregarEnv(Path.of(".env"));

    private Config() {
    }

    public static String get(String chave, String padrao) {
        String valor = System.getenv(chave);
        if (valor == null || valor.isBlank()) {
            valor = ARQUIVO_ENV.getProperty(chave);
        }
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    public static String obrigatoria(String chave) {
        String valor = get(chave, null);
        if (valor == null) {
            throw new IllegalStateException("Variável de ambiente " + chave + " não definida");
        }
        return valor;
    }

    private static Properties carregarEnv(Path arquivo) {
        Properties props = new Properties();
        if (Files.isRegularFile(arquivo)) {
            try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível ler o arquivo .env", e);
            }
        }
        return props;
    }
}
