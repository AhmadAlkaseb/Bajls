package app.dao;

import java.util.List;

public class EmptyEntityRepository<T> implements EntityRepository<T> {
    @Override
    public List<T> findAll() {
        return List.of();
    }

    @Override
    public T findById(Long id) {
        return null;
    }

    @Override
    public T save(T entity) {
        return entity;
    }

    @Override
    public T update(T entity) {
        return entity;
    }

    @Override
    public void deleteById(Long id) {
    }
}
