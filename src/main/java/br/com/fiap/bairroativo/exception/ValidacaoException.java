package br.com.fiap.bairroativo.exception;

/** Entrada inválida enviada pelo usuário (campo vazio, formato errado, tamanho excedido...). */
public class ValidacaoException extends AppException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 400;
    }
}
