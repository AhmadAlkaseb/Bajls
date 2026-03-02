package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.SkinColor;
public class SkinColorDao extends AbstractJpaDao<SkinColor> {
    public SkinColorDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, SkinColor.class);
    }
}
