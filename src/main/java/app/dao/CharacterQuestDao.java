package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.CharacterQuest;

public class CharacterQuestDao extends AbstractJpaDao<CharacterQuest> {
    public CharacterQuestDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, CharacterQuest.class);
    }
}
