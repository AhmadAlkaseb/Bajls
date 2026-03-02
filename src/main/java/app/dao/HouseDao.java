package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.House;
public class HouseDao extends AbstractJpaDao<House> {
    public HouseDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, House.class);
    }
}
