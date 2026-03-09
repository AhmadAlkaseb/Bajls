package app.dao;

import jakarta.persistence.EntityManagerFactory;
import persistence.entity.CharacterDrug;

public class CharacterDrugDao extends AbstractJpaDao<CharacterDrug> {
    public CharacterDrugDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, CharacterDrug.class);
    }
}
