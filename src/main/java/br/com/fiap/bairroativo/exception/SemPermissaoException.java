package br.com.fiap.bairroativo.exception;

/** O usuário entrou, mas o perfil dele não pode fazer esta ação. */
public class SemPermissaoException extends AppException {

    public SemPermissaoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 403;
    }
}
