package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Weight;
public class WeightDao extends AbstractJpaDao<Weight> {
    public WeightDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Weight.class);
    }
}
