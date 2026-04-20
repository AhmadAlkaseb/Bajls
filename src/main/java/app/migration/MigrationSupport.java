package app.migration;

import app.dao.EntityRepository;

import java.util.List;

public final class MigrationSupport {
    private MigrationSupport() {
    }

    public static <T> void saveAll(EntityRepository<T> repository, List<T> entities) {
        for (T entity : entities) {
            repository.save(entity);
        }
    }
}
