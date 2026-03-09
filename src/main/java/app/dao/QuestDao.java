package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Quest;

public class QuestDao extends AbstractJpaDao<Quest> {
    public QuestDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Quest.class);
    }
}
