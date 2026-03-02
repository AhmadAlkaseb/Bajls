package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Gang;
public class GangDao extends AbstractJpaDao<Gang> {
    public GangDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Gang.class);
    }
}
