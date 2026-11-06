package br.com.fiap.bairroativo.dao;

import java.util.List;
import java.util.Optional;

/** Contrato das operações CRUD que todo DAO de entidade principal implementa. */
public interface CrudDAO<T> {

    /** Grava a entidade e devolve ela com o id gerado pelo banco. */
    T inserir(T entidade);

    List<T> listar();

    Optional<T> buscarPorId(long id);

    /** @return false se o registro não existe. */
    boolean atualizar(T entidade);

    /** @return false se o registro não existe. */
    boolean excluir(long id);
}
