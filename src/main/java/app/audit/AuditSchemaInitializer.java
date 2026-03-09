package app.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public final class AuditSchemaInitializer {

    private AuditSchemaInitializer() {
    }

    public static void initialize(EntityManagerFactory entityManagerFactory) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("""
                    CREATE OR REPLACE FUNCTION fn_prevent_audit_log_changes()
                    RETURNS trigger
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        RAISE EXCEPTION 'audit_log is append-only and cannot be %', TG_OP;
                    END;
                    $$;
                    """).executeUpdate();
            em.createNativeQuery("DROP TRIGGER IF EXISTS trg_prevent_audit_log_update ON audit_log").executeUpdate();
            em.createNativeQuery("""
                    CREATE TRIGGER trg_prevent_audit_log_update
                    BEFORE UPDATE ON audit_log
                    FOR EACH ROW
                    EXECUTE FUNCTION fn_prevent_audit_log_changes()
                    """).executeUpdate();
            em.createNativeQuery("DROP TRIGGER IF EXISTS trg_prevent_audit_log_delete ON audit_log").executeUpdate();
            em.createNativeQuery("""
                    CREATE TRIGGER trg_prevent_audit_log_delete
                    BEFORE DELETE ON audit_log
                    FOR EACH ROW
                    EXECUTE FUNCTION fn_prevent_audit_log_changes()
                    """).executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
