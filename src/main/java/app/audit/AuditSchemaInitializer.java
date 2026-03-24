package app.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public final class AuditSchemaInitializer {

    private AuditSchemaInitializer() {
    }

    public static void initialize(EntityManagerFactory entityManagerFactory) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            executeStatement(em, """
                    CREATE OR REPLACE FUNCTION fn_prevent_audit_log_changes()
                    RETURNS trigger
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        RAISE EXCEPTION 'audit_log is append-only and cannot be %', TG_OP;
                    END;
                    $$;
                    """);
            executeStatement(em, "DROP TRIGGER IF EXISTS trg_prevent_audit_log_update ON audit_log");
            executeStatement(em, """
                    CREATE TRIGGER trg_prevent_audit_log_update
                    BEFORE UPDATE ON audit_log
                    FOR EACH ROW
                    EXECUTE FUNCTION fn_prevent_audit_log_changes()
                    """);
            executeStatement(em, "DROP TRIGGER IF EXISTS trg_prevent_audit_log_delete ON audit_log");
            executeStatement(em, """
                    CREATE TRIGGER trg_prevent_audit_log_delete
                    BEFORE DELETE ON audit_log
                    FOR EACH ROW
                    EXECUTE FUNCTION fn_prevent_audit_log_changes()
                    """);
            tx.commit();
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    private static void executeStatement(EntityManager em, String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }

    private static void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
