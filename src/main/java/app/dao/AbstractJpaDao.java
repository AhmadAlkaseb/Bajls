package app.dao;

import app.audit.AuditAction;
import app.audit.AuditContext;
import app.audit.AuditSnapshotUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaQuery;
import persistence.entity.AuditLog;
import persistence.entity.Profile;

import java.util.List;

public abstract class AbstractJpaDao<T> implements EntityRepository<T> {

    private final EntityManagerFactory entityManagerFactory;
    private final Class<T> entityClass;

    protected AbstractJpaDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
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
            recordAuditLog(em, AuditAction.CREATE, null, AuditSnapshotUtil.toJson(entity), entity);
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
            String previousStateJson = null;
            Long entityId = AuditSnapshotUtil.getEntityId(entity);
            if (entityId != null) {
                T existing = em.find(entityClass, entityId);
                previousStateJson = AuditSnapshotUtil.toJson(existing);
            }
            T merged = em.merge(entity);
            em.flush();
            recordAuditLog(em, AuditAction.UPDATE, previousStateJson, AuditSnapshotUtil.toJson(merged), merged);
            tx.commit();
            return entity;
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
                recordAuditLog(em, AuditAction.DELETE, AuditSnapshotUtil.toJson(entity), null, entity);
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

    private void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    private void recordAuditLog(EntityManager em, AuditAction action, String previousStateJson, String currentStateJson, T targetEntity) {
        if (AuditLog.class.equals(entityClass)) {
            return;
        }

        AuditContext.AuditMetadata metadata = AuditContext.getCurrent();
        if (metadata == null) {
            return;
        }

        Profile actorProfile = null;
        if (metadata.getActorProfileId() != null) {
            actorProfile = em.getReference(Profile.class, metadata.getActorProfileId());
        }

        AuditLog auditLog = AuditLog.builder()
                .actorProfile(actorProfile)
                .actorUsername(metadata.getActorUsername())
                .actorRole(metadata.getActorRole())
                .action(action.name())
                .entityName(entityClass.getSimpleName())
                .entityId(AuditSnapshotUtil.getEntityId(targetEntity))
                .requestMethod(metadata.getRequestMethod())
                .requestPath(metadata.getRequestPath())
                .oldValues(previousStateJson)
                .newValues(currentStateJson)
                .build();

        em.persist(auditLog);
    }
}
