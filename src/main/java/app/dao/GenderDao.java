package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Gender;
public class GenderDao extends AbstractJpaDao<Gender> {
    public GenderDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Gender.class);
    }
}
