package br.com.fiap.bairroativo.exception;

public class RegraNegocioException extends AppException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 409;
    }
}
