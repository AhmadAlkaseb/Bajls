package app.dao;

import java.util.List;

public interface ReadRepository<T> {
    List<T> findAll();

    T findById(Long id);
}
