package app.migration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import persistence.HibernateConfig;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.Profile;
import persistence.entity.Quest;
import persistence.entity.Vehicle;

public final class PostgresSnapshotLoader {
    private PostgresSnapshotLoader() {
    }

    public static MigrationSnapshot load(boolean isTest) {
        EntityManagerFactory factory = HibernateConfig.getEntityManagerFactoryConfig(isTest);
        try (EntityManager em = factory.createEntityManager()) {
            var profiles = EntityCopies.profiles(em.createQuery(
                    "SELECT p FROM Profile p ORDER BY p.id", Profile.class
            ).getResultList());
            var drugs = EntityCopies.drugs(em.createQuery(
                    "SELECT d FROM Drug d ORDER BY d.id", Drug.class
            ).getResultList());
            var quests = EntityCopies.quests(em.createQuery(
                    "SELECT q FROM Quest q ORDER BY q.id", Quest.class
            ).getResultList());
            var gangs = EntityCopies.gangs(em.createQuery(
                    "SELECT g FROM Gang g ORDER BY g.id", Gang.class
            ).getResultList());
            var characters = EntityCopies.characters(em.createQuery("""
                    SELECT c FROM GameCharacter c
                    JOIN FETCH c.profile
                    JOIN FETCH c.house
                    JOIN FETCH c.garage
                    ORDER BY c.id
                    """, GameCharacter.class).getResultList());
            var houses = characters.stream()
                    .map(character -> EntityCopies.houseWithCharacter(character.getHouse(), character.getId()))
                    .toList();
            var garages = characters.stream()
                    .map(character -> EntityCopies.garageWithCharacter(character.getGarage(), character.getId()))
                    .toList();
            var vehicles = EntityCopies.vehicles(em.createQuery("""
                    SELECT v FROM Vehicle v
                    JOIN FETCH v.garage
                    ORDER BY v.id
                    """, Vehicle.class).getResultList());
            var characterDrugs = EntityCopies.characterDrugs(em.createQuery("""
                    SELECT cd FROM CharacterDrug cd
                    JOIN FETCH cd.character
                    JOIN FETCH cd.drug
                    ORDER BY cd.id
                    """, CharacterDrug.class).getResultList());
            var characterQuests = EntityCopies.characterQuests(em.createQuery("""
                    SELECT cq FROM CharacterQuest cq
                    JOIN FETCH cq.character
                    JOIN FETCH cq.quest
                    ORDER BY cq.id
                    """, CharacterQuest.class).getResultList());
            var gangAffiliations = EntityCopies.gangAffiliations(em.createQuery("""
                    SELECT ga FROM GangAffiliation ga
                    JOIN FETCH ga.character
                    JOIN FETCH ga.gang
                    ORDER BY ga.id
                    """, GangAffiliation.class).getResultList());

            return new MigrationSnapshot(
                    profiles,
                    drugs,
                    quests,
                    gangs,
                    houses,
                    garages,
                    characters,
                    vehicles,
                    characterDrugs,
                    characterQuests,
                    gangAffiliations
            );
        }
    }
}
