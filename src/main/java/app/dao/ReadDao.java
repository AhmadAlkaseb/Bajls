package app.dao;

import java.util.List;
import java.util.Optional;

public interface ReadDao<T> {
    List<T> findAll();
    Optional<T> findById(Integer id);
}
