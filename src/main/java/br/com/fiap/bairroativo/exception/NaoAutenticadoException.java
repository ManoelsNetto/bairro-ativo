package br.com.fiap.bairroativo.exception;

/** A ação exige entrar na plataforma com um perfil (cidadão ou servidor). */
public class NaoAutenticadoException extends AppException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 401;
    }
}
