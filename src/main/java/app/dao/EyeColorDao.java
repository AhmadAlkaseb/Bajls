package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.EyeColor;
public class EyeColorDao extends AbstractJpaDao<EyeColor> {
    public EyeColorDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, EyeColor.class);
    }
}
