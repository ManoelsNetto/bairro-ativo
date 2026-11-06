package br.com.fiap.bairroativo.config;

import br.com.fiap.bairroativo.exception.AppException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.server.ParamException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converte qualquer exceção em uma resposta JSON com mensagem clara para a tela.
 * Nenhum erro derruba o servidor: o pior caso é um 500 com mensagem genérica (o detalhe vai para o log).
 */
public class ErroHandler implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(ErroHandler.class.getName());

    public record Erro(int status, String mensagem, String caminho) {
    }

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof AppException app) {
            if (app.getStatusHttp() >= 500) {
                log.log(Level.SEVERE, app.getMessage(), app.getCause());
            }
            return erro(app.getStatusHttp(), app.getMessage());
        }
        if (e instanceof ParamException) {
            return erro(400, "Parâmetro inválido na requisição.");
        }
        if (e instanceof JsonProcessingException || e.getCause() instanceof JsonProcessingException) {
            return erro(400, "Dados enviados em formato inválido.");
        }
        if (e instanceof WebApplicationException web) {
            int status = web.getResponse().getStatus();
            return erro(status, switch (status) {
                case 404 -> "Recurso não encontrado.";
                case 405 -> "Operação não permitida neste endereço.";
                case 415 -> "Envie os dados em JSON.";
                default -> "Requisição inválida.";
            });
        }
        log.log(Level.SEVERE, "Erro inesperado", e);
        return erro(500, "Ocorreu um erro inesperado. Tente novamente.");
    }

    private Response erro(int status, String mensagem) {
        String caminho = uriInfo == null ? null : "/" + uriInfo.getPath();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8"))
                .entity(new Erro(status, mensagem, caminho))
                .build();
    }
}
