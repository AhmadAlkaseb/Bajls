package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class JpaReadDao<T> {

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

    public List<T> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(listJpql, dtoClass).getResultList();
        } finally {
            em.close();
        }
    }

    public T findById(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(byIdJpql, dtoClass)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
}
