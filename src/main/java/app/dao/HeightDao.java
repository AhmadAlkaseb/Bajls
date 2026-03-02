package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Height;
public class HeightDao extends AbstractJpaDao<Height> {
    public HeightDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Height.class);
    }
}
