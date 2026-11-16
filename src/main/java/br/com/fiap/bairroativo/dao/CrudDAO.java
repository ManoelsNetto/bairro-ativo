package br.com.fiap.bairroativo.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {

    T inserir(T entidade);

    List<T> listar();

    Optional<T> buscarPorId(long id);

    boolean atualizar(T entidade);

    boolean excluir(long id);
}
