package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Vehicle;

public class VehicleDao extends AbstractJpaDao<Vehicle> {
    public VehicleDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Vehicle.class);
    }
}
