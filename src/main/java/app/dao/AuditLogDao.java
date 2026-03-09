package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.AuditLog;

public class AuditLogDao extends AbstractJpaDao<AuditLog> {
    public AuditLogDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, AuditLog.class);
    }
}
