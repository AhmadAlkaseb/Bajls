package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;
public class ProfileDao extends AbstractJpaDao<Profile> {
    public ProfileDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Profile.class);
    }
}
