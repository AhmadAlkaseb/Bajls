package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public class JpaReadDao<T> implements ReadDao<T> {

    private final EntityManagerFactory entityManagerFactory;
    private final String listJpql;
    private final String byIdJpql;
    private final Class<T> dtoClass;

    public JpaReadDao(EntityManagerFactory entityManagerFactory, String listJpql, String byIdJpql, Class<T> dtoClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.listJpql = listJpql;
        this.byIdJpql = byIdJpql;
        this.dtoClass = dtoClass;
    }

    @Override
    public List<T> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(listJpql, dtoClass).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<T> findById(Integer id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(byIdJpql, dtoClass)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
