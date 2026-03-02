package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.GameCharacter;
public class GameCharacterDao extends AbstractJpaDao<GameCharacter> {
    public GameCharacterDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, GameCharacter.class);
    }
}
