package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;
import java.util.Optional;

public abstract class AbstractJpaDao<T> implements GenericDao<T, Integer> {

    private final EntityManagerFactory entityManagerFactory;
    private final Class<T> entityClass;

    protected AbstractJpaDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityClass = entityClass;
    }

    @Override
    public T save(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public T update(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Integer id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<T> findById(Integer id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            CriteriaQuery<T> criteria = em.getCriteriaBuilder().createQuery(entityClass);
            criteria.from(entityClass);
            return em.createQuery(criteria).getResultList();
        } finally {
            em.close();
        }
    }

    private void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
