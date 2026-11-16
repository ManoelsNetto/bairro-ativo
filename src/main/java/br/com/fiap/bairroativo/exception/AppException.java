package br.com.fiap.bairroativo.exception;

public abstract class AppException extends RuntimeException {

    protected AppException(String mensagem) {
        super(mensagem);
    }

    protected AppException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public abstract int getStatusHttp();
}
