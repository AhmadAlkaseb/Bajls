package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Garage;

public class GarageDao extends AbstractJpaDao<Garage> {
    public GarageDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Garage.class);
    }
}
