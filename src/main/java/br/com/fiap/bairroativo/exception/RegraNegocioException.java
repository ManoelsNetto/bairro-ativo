package br.com.fiap.bairroativo.exception;

/** A operação é válida no formato, mas viola uma regra do sistema (ex.: apoiar a própria ocorrência). */
public class RegraNegocioException extends AppException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int getStatusHttp() {
        return 409;
    }
}
