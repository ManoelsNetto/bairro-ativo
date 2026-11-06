package br.com.fiap.bairroativo.exception;

/** Registro procurado não existe no banco. */
public class NaoEncontradoException extends AppException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 404;
    }
}
