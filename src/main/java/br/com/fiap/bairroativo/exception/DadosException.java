package br.com.fiap.bairroativo.exception;

import java.sql.SQLException;

/**
 * Falha ao acessar o banco. O construtor {@link #de(String, SQLException)} traduz os códigos de erro do Oracle
 * em mensagens claras; violações de restrição viram {@link RegraNegocioException}.
 */
public class DadosException extends AppException {

    private final int statusHttp;

    public DadosException(String mensagem, Throwable causa, int statusHttp) {
        super(mensagem, causa);
        this.statusHttp = statusHttp;
    }

    @Override
    public int getStatusHttp() {
        return statusHttp;
    }

    public static AppException de(String operacao, SQLException e) {
        return switch (e.getErrorCode()) {
            case 1 -> new RegraNegocioException(mensagemUnica(e.getMessage()));
            case 2292 -> new RegraNegocioException(mensagemVinculo(e.getMessage()));
            case 2291 -> new RegraNegocioException("Um dos registros relacionados não existe mais.");
            case 2290 -> new RegraNegocioException("Valor fora das opções permitidas.");
            case 1400 -> new RegraNegocioException("Preencha todos os campos obrigatórios.");
            case 12899 -> new RegraNegocioException("Um dos campos passou do tamanho máximo permitido.");
            case 1017, 17002, 17008, 12514, 12541 -> new DadosException(
                    "Banco de dados indisponível no momento. Tente novamente em instantes.", e, 503);
            default -> new DadosException("Erro ao " + operacao + ". Tente novamente.", e, 500);
        };
    }

    private static String mensagemVinculo(String mensagemOracle) {
        String msg = mensagemOracle == null ? "" : mensagemOracle.toUpperCase();
        if (msg.contains("FK_BA_OCORRENCIA_AUTOR")) {
            return "Não é possível excluir: este cidadão registrou ocorrências.";
        }
        if (msg.contains("FK_BA_ATUALIZACAO_AGENTE")) {
            return "Não é possível excluir: este agente já atendeu ocorrências e faz parte do histórico.";
        }
        if (msg.contains("FK_BA_USUARIO_BAIRRO")) {
            return "Não é possível excluir: há moradores cadastrados neste bairro.";
        }
        if (msg.contains("FK_BA_OCORRENCIA_BAIRRO")) {
            return "Não é possível excluir: há ocorrências registradas neste bairro.";
        }
        if (msg.contains("FK_BA_OCORRENCIA_CATEGORIA")) {
            return "Não é possível excluir: há ocorrências nesta categoria.";
        }
        return "Não é possível excluir: existem registros vinculados a este item.";
    }

    private static String mensagemUnica(String mensagemOracle) {
        String msg = mensagemOracle == null ? "" : mensagemOracle.toUpperCase();
        if (msg.contains("UK_BA_USUARIO_EMAIL")) {
            return "Já existe um usuário com este e-mail.";
        }
        if (msg.contains("UK_BA_USUARIO_MATRICULA")) {
            return "Já existe um agente com esta matrícula.";
        }
        if (msg.contains("UK_BA_BAIRRO_NOME")) {
            return "Já existe um bairro com este nome nesta cidade.";
        }
        if (msg.contains("UK_BA_CATEGORIA_NOME")) {
            return "Já existe uma categoria com este nome.";
        }
        if (msg.contains("PK_BA_APOIO")) {
            return "Este cidadão já apoiou esta ocorrência.";
        }
        return "Registro duplicado: já existe um cadastro com estes dados.";
    }
}
