package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.GameCharacterDTO;
import jakarta.persistence.EntityManagerFactory;

public class GameCharacterReadDao extends JpaReadDao<GameCharacterDTO> {
    public GameCharacterReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c",
                "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c WHERE c.id = :id",
                GameCharacterDTO.class
        );
    }
}
