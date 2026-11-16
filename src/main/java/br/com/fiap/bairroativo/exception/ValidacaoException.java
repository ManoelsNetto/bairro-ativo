package br.com.fiap.bairroativo.exception;

public class ValidacaoException extends AppException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 400;
    }
}
