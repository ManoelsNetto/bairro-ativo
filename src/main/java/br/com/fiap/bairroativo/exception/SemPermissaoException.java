package br.com.fiap.bairroativo.exception;

public class SemPermissaoException extends AppException {

    public SemPermissaoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 403;
    }
}
