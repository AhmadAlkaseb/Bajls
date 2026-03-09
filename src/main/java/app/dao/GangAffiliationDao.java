package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.GangAffiliation;

public class GangAffiliationDao extends AbstractJpaDao<GangAffiliation> {
    public GangAffiliationDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, GangAffiliation.class);
    }
}
