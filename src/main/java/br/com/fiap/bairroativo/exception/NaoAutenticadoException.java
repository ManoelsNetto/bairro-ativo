package br.com.fiap.bairroativo.exception;

public class NaoAutenticadoException extends AppException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 401;
    }
}
