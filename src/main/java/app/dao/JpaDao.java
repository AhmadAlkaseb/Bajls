package app.dao;

import jakarta.persistence.EntityManagerFactory;

public class JpaDao<T> extends AbstractJpaDao<T> {

    public JpaDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
        super(entityManagerFactory, entityClass);
    }
}
