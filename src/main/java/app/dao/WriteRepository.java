package app.dao;

public interface WriteRepository<T> {
    T save(T entity);

    T update(T entity);

    void deleteById(Long id);
}
