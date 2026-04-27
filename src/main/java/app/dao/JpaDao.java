package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;

public class JpaDao<T> implements EntityRepository<T> {
    private final EntityManagerFactory entityManagerFactory;
    private final Class<T> entityClass;

    public JpaDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            em.persist(entity);
            em.flush();
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public T update(T entity) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            T merged = em.merge(entity);
            em.flush();
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public void deleteById(Long id) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public T findById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            CriteriaQuery<T> criteria = em.getCriteriaBuilder().createQuery(entityClass);
            criteria.from(entityClass);
            return em.createQuery(criteria).getResultList();
        }
    }

    protected EntityManagerFactory entityManagerFactory() {
        return entityManagerFactory;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    private void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
