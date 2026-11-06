package br.com.fiap.bairroativo.exception;

/** Base das exceções da aplicação: cada uma sabe qual status HTTP representa e traz uma mensagem para o usuário. */
public abstract class AppException extends RuntimeException {

    protected AppException(String mensagem) {
        super(mensagem);
    }

    protected AppException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public abstract int getStatusHttp();
}
