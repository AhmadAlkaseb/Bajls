package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Drug;

public class DrugDao extends AbstractJpaDao<Drug> {
    public DrugDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Drug.class);
    }
}
