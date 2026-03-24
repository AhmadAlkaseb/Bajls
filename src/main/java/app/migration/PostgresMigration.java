package app.migration;

import app.dao.EntityRepository;
import app.mongo.MongoEntityRepository;
import app.mongo.MongoProfileRepository;
import app.neo4j.Neo4jEntityRepository;
import app.neo4j.Neo4jProfileRepository;
import app.neo4j.Neo4jSequenceRepository;
import app.neo4j.Neo4jSupport;
import app.setup.DatabaseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import persistence.HibernateConfig;
import persistence.entity.AuditLog;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.Garage;
import persistence.entity.House;
import persistence.entity.Profile;
import persistence.entity.Quest;
import persistence.entity.Vehicle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PostgresMigration {
    private static final String DEFAULT_MONGO_URL = "mongodb://localhost:27017";
    private static final String DEFAULT_MONGO_DATABASE = "bajls";
    private static final String DEFAULT_NEO4J_URI = "bolt://localhost:7687";
    private static final String DEFAULT_NEO4J_USER = "neo4j";
    private static final String DEFAULT_NEO4J_PASSWORD = "password";

    private PostgresMigration() {
    }

    public static void migrate(DatabaseType targetDatabaseType, boolean isTest) {
        if (targetDatabaseType == DatabaseType.POSTGRES) {
            return;
        }

        MigrationSnapshot snapshot = loadSnapshot(isTest);
        try (MigrationTarget target = createTarget(targetDatabaseType)) {
            target.clear();
            migrateSnapshot(snapshot, target);
        }
    }

    private static MigrationSnapshot loadSnapshot(boolean isTest) {
        EntityManagerFactory entityManagerFactory = HibernateConfig.getEntityManagerFactoryConfig(isTest);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Profile> profiles = copyProfiles(entityManager.createQuery(
                    "SELECT p FROM Profile p ORDER BY p.id",
                    Profile.class
            ).getResultList());

            List<Drug> drugs = copyDrugs(entityManager.createQuery(
                    "SELECT d FROM Drug d ORDER BY d.id",
                    Drug.class
            ).getResultList());

            List<Quest> quests = copyQuests(entityManager.createQuery(
                    "SELECT q FROM Quest q ORDER BY q.id",
                    Quest.class
            ).getResultList());

            List<Gang> gangs = copyGangs(entityManager.createQuery(
                    "SELECT g FROM Gang g ORDER BY g.id",
                    Gang.class
            ).getResultList());

            List<GameCharacter> characters = copyCharacters(entityManager.createQuery(
                    """
                    SELECT c
                    FROM GameCharacter c
                    JOIN FETCH c.profile
                    JOIN FETCH c.house
                    JOIN FETCH c.garage
                    ORDER BY c.id
                    """,
                    GameCharacter.class
            ).getResultList());

            List<House> houses = characters.stream()
                    .map(character -> copyHouseWithCharacter(character.getHouse(), character.getId()))
                    .toList();

            List<Garage> garages = characters.stream()
                    .map(character -> copyGarageWithCharacter(character.getGarage(), character.getId()))
                    .toList();

            List<Vehicle> vehicles = copyVehicles(entityManager.createQuery(
                    """
                    SELECT v
                    FROM Vehicle v
                    JOIN FETCH v.garage
                    ORDER BY v.id
                    """,
                    Vehicle.class
            ).getResultList());

            List<CharacterDrug> characterDrugs = copyCharacterDrugs(entityManager.createQuery(
                    """
                    SELECT cd
                    FROM CharacterDrug cd
                    JOIN FETCH cd.character
                    JOIN FETCH cd.drug
                    ORDER BY cd.id
                    """,
                    CharacterDrug.class
            ).getResultList());

            List<CharacterQuest> characterQuests = copyCharacterQuests(entityManager.createQuery(
                    """
                    SELECT cq
                    FROM CharacterQuest cq
                    JOIN FETCH cq.character
                    JOIN FETCH cq.quest
                    ORDER BY cq.id
                    """,
                    CharacterQuest.class
            ).getResultList());

            List<GangAffiliation> gangAffiliations = copyGangAffiliations(entityManager.createQuery(
                    """
                    SELECT ga
                    FROM GangAffiliation ga
                    JOIN FETCH ga.character
                    JOIN FETCH ga.gang
                    ORDER BY ga.id
                    """,
                    GangAffiliation.class
            ).getResultList());

            List<AuditLog> auditLogs = copyAuditLogs(entityManager.createQuery(
                    "SELECT a FROM AuditLog a ORDER BY a.id",
                    AuditLog.class
            ).getResultList());

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
                    gangAffiliations,
                    auditLogs
            );
        }
    }

    private static MigrationTarget createTarget(DatabaseType databaseType) {
        return switch (databaseType) {
            case MONGODB -> new MongoMigrationTarget();
            case NEO4J -> new Neo4jMigrationTarget();
            case POSTGRES -> throw new IllegalArgumentException("Postgres is the source database, not a migration target");
        };
    }

    private static void migrateSnapshot(MigrationSnapshot snapshot, MigrationTarget target) {
        saveAll(target.profileRepository(), snapshot.profiles());
        saveAll(target.drugRepository(), snapshot.drugs());
        saveAll(target.questRepository(), snapshot.quests());
        saveAll(target.gangRepository(), snapshot.gangs());
        saveAll(target.houseRepository(), snapshot.houses());
        saveAll(target.garageRepository(), snapshot.garages());
        saveAll(target.characterRepository(), snapshot.characters());
        saveAll(target.vehicleRepository(), snapshot.vehicles());
        saveAll(target.characterDrugRepository(), snapshot.characterDrugs());
        saveAll(target.characterQuestRepository(), snapshot.characterQuests());
        saveAll(target.gangAffiliationRepository(), snapshot.gangAffiliations());
        saveAll(target.auditLogRepository(), snapshot.auditLogs());
    }

    private static <T> void saveAll(EntityRepository<T> repository, List<T> entities) {
        for (T entity : entities) {
            repository.save(entity);
        }
    }

    private static List<Profile> copyProfiles(List<Profile> sourceProfiles) {
        List<Profile> profiles = new ArrayList<>();
        for (Profile sourceProfile : sourceProfiles) {
            profiles.add(Profile.builder()
                    .id(sourceProfile.getId())
                    .firstName(sourceProfile.getFirstName())
                    .lastName(sourceProfile.getLastName())
                    .email(sourceProfile.getEmail())
                    .username(sourceProfile.getUsername())
                    .password(sourceProfile.getPassword())
                    .role(sourceProfile.getRole())
                    .characters(new ArrayList<>())
                    .build());
        }
        return profiles;
    }

    private static List<Drug> copyDrugs(List<Drug> sourceDrugs) {
        List<Drug> drugs = new ArrayList<>();
        for (Drug sourceDrug : sourceDrugs) {
            drugs.add(Drug.builder()
                    .id(sourceDrug.getId())
                    .name(sourceDrug.getName())
                    .type(sourceDrug.getType())
                    .characterDrugs(new ArrayList<>())
                    .build());
        }
        return drugs;
    }

    private static List<Quest> copyQuests(List<Quest> sourceQuests) {
        List<Quest> quests = new ArrayList<>();
        for (Quest sourceQuest : sourceQuests) {
            quests.add(Quest.builder()
                    .id(sourceQuest.getId())
                    .title(sourceQuest.getTitle())
                    .description(sourceQuest.getDescription())
                    .reward(sourceQuest.getReward() == null ? BigDecimal.ZERO : sourceQuest.getReward())
                    .characterQuests(new ArrayList<>())
                    .build());
        }
        return quests;
    }

    private static List<Gang> copyGangs(List<Gang> sourceGangs) {
        List<Gang> gangs = new ArrayList<>();
        for (Gang sourceGang : sourceGangs) {
            gangs.add(Gang.builder()
                    .id(sourceGang.getId())
                    .name(sourceGang.getName())
                    .type(sourceGang.getType())
                    .affiliations(new ArrayList<>())
                    .build());
        }
        return gangs;
    }

    private static List<GameCharacter> copyCharacters(List<GameCharacter> sourceCharacters) {
        List<GameCharacter> characters = new ArrayList<>();
        for (GameCharacter sourceCharacter : sourceCharacters) {
            characters.add(GameCharacter.builder()
                    .id(sourceCharacter.getId())
                    .name(sourceCharacter.getName())
                    .balance(sourceCharacter.getBalance() == null ? BigDecimal.ZERO : sourceCharacter.getBalance())
                    .profile(copyProfileReference(sourceCharacter.getProfile()))
                    .gender(sourceCharacter.getGender())
                    .skincolor(sourceCharacter.getSkincolor())
                    .eyecolor(sourceCharacter.getEyecolor())
                    .height(sourceCharacter.getHeight())
                    .weight(sourceCharacter.getWeight())
                    .house(copyHouseReference(sourceCharacter.getHouse()))
                    .garage(copyGarageReference(sourceCharacter.getGarage()))
                    .characterDrugs(new ArrayList<>())
                    .characterQuests(new ArrayList<>())
                    .gangAffiliations(new ArrayList<>())
                    .build());
        }
        return characters;
    }

    private static List<Vehicle> copyVehicles(List<Vehicle> sourceVehicles) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle sourceVehicle : sourceVehicles) {
            vehicles.add(Vehicle.builder()
                    .id(sourceVehicle.getId())
                    .garage(copyGarageReference(sourceVehicle.getGarage()))
                    .model(sourceVehicle.getModel())
                    .type(sourceVehicle.getType())
                    .plateNumber(sourceVehicle.getPlateNumber())
                    .build());
        }
        return vehicles;
    }

    private static List<CharacterDrug> copyCharacterDrugs(List<CharacterDrug> sourceCharacterDrugs) {
        List<CharacterDrug> characterDrugs = new ArrayList<>();
        for (CharacterDrug sourceCharacterDrug : sourceCharacterDrugs) {
            characterDrugs.add(CharacterDrug.builder()
                    .id(sourceCharacterDrug.getId())
                    .character(copyCharacterReference(sourceCharacterDrug.getCharacter()))
                    .drug(copyDrugReference(sourceCharacterDrug.getDrug()))
                    .quantity(sourceCharacterDrug.getQuantity())
                    .build());
        }
        return characterDrugs;
    }

    private static List<CharacterQuest> copyCharacterQuests(List<CharacterQuest> sourceCharacterQuests) {
        List<CharacterQuest> characterQuests = new ArrayList<>();
        for (CharacterQuest sourceCharacterQuest : sourceCharacterQuests) {
            characterQuests.add(CharacterQuest.builder()
                    .id(sourceCharacterQuest.getId())
                    .character(copyCharacterReference(sourceCharacterQuest.getCharacter()))
                    .quest(copyQuestReference(sourceCharacterQuest.getQuest()))
                    .status(sourceCharacterQuest.getStatus())
                    .acceptedAt(sourceCharacterQuest.getAcceptedAt())
                    .build());
        }
        return characterQuests;
    }

    private static List<GangAffiliation> copyGangAffiliations(List<GangAffiliation> sourceGangAffiliations) {
        List<GangAffiliation> gangAffiliations = new ArrayList<>();
        for (GangAffiliation sourceGangAffiliation : sourceGangAffiliations) {
            gangAffiliations.add(GangAffiliation.builder()
                    .id(sourceGangAffiliation.getId())
                    .character(copyCharacterReference(sourceGangAffiliation.getCharacter()))
                    .gang(copyGangReference(sourceGangAffiliation.getGang()))
                    .joinDate(sourceGangAffiliation.getJoinDate())
                    .build());
        }
        return gangAffiliations;
    }

    private static List<AuditLog> copyAuditLogs(List<AuditLog> sourceAuditLogs) {
        List<AuditLog> auditLogs = new ArrayList<>();
        for (AuditLog sourceAuditLog : sourceAuditLogs) {
            auditLogs.add(AuditLog.builder()
                    .id(sourceAuditLog.getId())
                    .actorProfile(sourceAuditLog.getActorProfileId() == null ? null : Profile.builder().id(sourceAuditLog.getActorProfileId()).characters(new ArrayList<>()).build())
                    .actorProfileId(sourceAuditLog.getActorProfileId())
                    .actorUsername(sourceAuditLog.getActorUsername())
                    .actorRole(sourceAuditLog.getActorRole())
                    .action(sourceAuditLog.getAction())
                    .entityName(sourceAuditLog.getEntityName())
                    .entityId(sourceAuditLog.getEntityId())
                    .requestMethod(sourceAuditLog.getRequestMethod())
                    .requestPath(sourceAuditLog.getRequestPath())
                    .oldValues(sourceAuditLog.getOldValues())
                    .newValues(sourceAuditLog.getNewValues())
                    .changedAt(sourceAuditLog.getChangedAt())
                    .build());
        }
        return auditLogs;
    }

    private static House copyHouseWithCharacter(House sourceHouse, Long characterId) {
        return House.builder()
                .id(sourceHouse.getId())
                .amountRooms(sourceHouse.getAmountRooms())
                .amountBathrooms(sourceHouse.getAmountBathrooms())
                .character(GameCharacter.builder().id(characterId).characterDrugs(new ArrayList<>()).characterQuests(new ArrayList<>()).gangAffiliations(new ArrayList<>()).build())
                .build();
    }

    private static Garage copyGarageWithCharacter(Garage sourceGarage, Long characterId) {
        return Garage.builder()
                .id(sourceGarage.getId())
                .capacity(sourceGarage.getCapacity())
                .character(GameCharacter.builder().id(characterId).characterDrugs(new ArrayList<>()).characterQuests(new ArrayList<>()).gangAffiliations(new ArrayList<>()).build())
                .vehicles(new ArrayList<>())
                .build();
    }

    private static Profile copyProfileReference(Profile sourceProfile) {
        return Profile.builder()
                .id(sourceProfile.getId())
                .characters(new ArrayList<>())
                .build();
    }

    private static House copyHouseReference(House sourceHouse) {
        return House.builder()
                .id(sourceHouse.getId())
                .amountRooms(sourceHouse.getAmountRooms())
                .amountBathrooms(sourceHouse.getAmountBathrooms())
                .build();
    }

    private static Garage copyGarageReference(Garage sourceGarage) {
        return Garage.builder()
                .id(sourceGarage.getId())
                .capacity(sourceGarage.getCapacity())
                .vehicles(new ArrayList<>())
                .build();
    }

    private static Drug copyDrugReference(Drug sourceDrug) {
        return Drug.builder()
                .id(sourceDrug.getId())
                .name(sourceDrug.getName())
                .type(sourceDrug.getType())
                .characterDrugs(new ArrayList<>())
                .build();
    }

    private static Quest copyQuestReference(Quest sourceQuest) {
        return Quest.builder()
                .id(sourceQuest.getId())
                .title(sourceQuest.getTitle())
                .description(sourceQuest.getDescription())
                .reward(sourceQuest.getReward())
                .characterQuests(new ArrayList<>())
                .build();
    }

    private static Gang copyGangReference(Gang sourceGang) {
        return Gang.builder()
                .id(sourceGang.getId())
                .name(sourceGang.getName())
                .type(sourceGang.getType())
                .affiliations(new ArrayList<>())
                .build();
    }

    private static GameCharacter copyCharacterReference(GameCharacter sourceCharacter) {
        return GameCharacter.builder()
                .id(sourceCharacter.getId())
                .name(sourceCharacter.getName())
                .balance(sourceCharacter.getBalance())
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build();
    }

    private record MigrationSnapshot(
            List<Profile> profiles,
            List<Drug> drugs,
            List<Quest> quests,
            List<Gang> gangs,
            List<House> houses,
            List<Garage> garages,
            List<GameCharacter> characters,
            List<Vehicle> vehicles,
            List<CharacterDrug> characterDrugs,
            List<CharacterQuest> characterQuests,
            List<GangAffiliation> gangAffiliations,
            List<AuditLog> auditLogs
    ) {
    }

    private interface MigrationTarget extends AutoCloseable {
        void clear();

        EntityRepository<Profile> profileRepository();

        EntityRepository<AuditLog> auditLogRepository();

        EntityRepository<Drug> drugRepository();

        EntityRepository<Quest> questRepository();

        EntityRepository<GameCharacter> characterRepository();

        EntityRepository<House> houseRepository();

        EntityRepository<Garage> garageRepository();

        EntityRepository<Vehicle> vehicleRepository();

        EntityRepository<CharacterDrug> characterDrugRepository();

        EntityRepository<CharacterQuest> characterQuestRepository();

        EntityRepository<Gang> gangRepository();

        EntityRepository<GangAffiliation> gangAffiliationRepository();

        @Override
        void close();
    }

    private static final class MongoMigrationTarget implements MigrationTarget {
        private final MongoClient mongoClient;
        private final MongoDatabase database;
        private final EntityRepository<Profile> profileRepository;
        private final EntityRepository<AuditLog> auditLogRepository;
        private final EntityRepository<Drug> drugRepository;
        private final EntityRepository<Quest> questRepository;
        private final EntityRepository<GameCharacter> characterRepository;
        private final EntityRepository<House> houseRepository;
        private final EntityRepository<Garage> garageRepository;
        private final EntityRepository<Vehicle> vehicleRepository;
        private final EntityRepository<CharacterDrug> characterDrugRepository;
        private final EntityRepository<CharacterQuest> characterQuestRepository;
        private final EntityRepository<Gang> gangRepository;
        private final EntityRepository<GangAffiliation> gangAffiliationRepository;

        private MongoMigrationTarget() {
            this.mongoClient = MongoClients.create(System.getenv().getOrDefault("MONGO_URL", DEFAULT_MONGO_URL));
            this.database = mongoClient.getDatabase(System.getenv().getOrDefault("MONGO_DB_NAME", DEFAULT_MONGO_DATABASE));
            ObjectMapper objectMapper = app.mongo.MongoSupport.createObjectMapper();
            this.profileRepository = new MongoProfileRepository(database, objectMapper);
            this.auditLogRepository = new MongoEntityRepository<>(database, "audit_log", AuditLog.class, objectMapper);
            this.drugRepository = new MongoEntityRepository<>(database, "drugs", Drug.class, objectMapper);
            this.questRepository = new MongoEntityRepository<>(database, "quests", Quest.class, objectMapper);
            this.characterRepository = new MongoEntityRepository<>(database, "characters", GameCharacter.class, objectMapper);
            this.houseRepository = new MongoEntityRepository<>(database, "houses", House.class, objectMapper);
            this.garageRepository = new MongoEntityRepository<>(database, "garages", Garage.class, objectMapper);
            this.vehicleRepository = new MongoEntityRepository<>(database, "vehicles", Vehicle.class, objectMapper);
            this.characterDrugRepository = new MongoEntityRepository<>(database, "character_drug", CharacterDrug.class, objectMapper);
            this.characterQuestRepository = new MongoEntityRepository<>(database, "character_quest", CharacterQuest.class, objectMapper);
            this.gangRepository = new MongoEntityRepository<>(database, "gangs", Gang.class, objectMapper);
            this.gangAffiliationRepository = new MongoEntityRepository<>(database, "gang_affiliations", GangAffiliation.class, objectMapper);
        }

        @Override
        public void clear() {
            database.drop();
        }

        @Override public EntityRepository<Profile> profileRepository() { return profileRepository; }
        @Override public EntityRepository<AuditLog> auditLogRepository() { return auditLogRepository; }
        @Override public EntityRepository<Drug> drugRepository() { return drugRepository; }
        @Override public EntityRepository<Quest> questRepository() { return questRepository; }
        @Override public EntityRepository<GameCharacter> characterRepository() { return characterRepository; }
        @Override public EntityRepository<House> houseRepository() { return houseRepository; }
        @Override public EntityRepository<Garage> garageRepository() { return garageRepository; }
        @Override public EntityRepository<Vehicle> vehicleRepository() { return vehicleRepository; }
        @Override public EntityRepository<CharacterDrug> characterDrugRepository() { return characterDrugRepository; }
        @Override public EntityRepository<CharacterQuest> characterQuestRepository() { return characterQuestRepository; }
        @Override public EntityRepository<Gang> gangRepository() { return gangRepository; }
        @Override public EntityRepository<GangAffiliation> gangAffiliationRepository() { return gangAffiliationRepository; }

        @Override
        public void close() {
            mongoClient.close();
        }
    }

    private static final class Neo4jMigrationTarget implements MigrationTarget {
        private final Driver driver;
        private final EntityRepository<Profile> profileRepository;
        private final EntityRepository<AuditLog> auditLogRepository;
        private final EntityRepository<Drug> drugRepository;
        private final EntityRepository<Quest> questRepository;
        private final EntityRepository<GameCharacter> characterRepository;
        private final EntityRepository<House> houseRepository;
        private final EntityRepository<Garage> garageRepository;
        private final EntityRepository<Vehicle> vehicleRepository;
        private final EntityRepository<CharacterDrug> characterDrugRepository;
        private final EntityRepository<CharacterQuest> characterQuestRepository;
        private final EntityRepository<Gang> gangRepository;
        private final EntityRepository<GangAffiliation> gangAffiliationRepository;

        private Neo4jMigrationTarget() {
            this.driver = GraphDatabase.driver(
                    System.getenv().getOrDefault("NEO4J_URI", DEFAULT_NEO4J_URI),
                    AuthTokens.basic(
                            System.getenv().getOrDefault("NEO4J_USER", DEFAULT_NEO4J_USER),
                            System.getenv().getOrDefault("NEO4J_PASSWORD", DEFAULT_NEO4J_PASSWORD)
                    )
            );
            driver.verifyConnectivity();
            ObjectMapper objectMapper = Neo4jSupport.createObjectMapper();
            Neo4jSequenceRepository sequenceRepository = new Neo4jSequenceRepository(driver);
            this.profileRepository = new Neo4jProfileRepository(driver, sequenceRepository, objectMapper);
            this.auditLogRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, AuditLog.class, objectMapper);
            this.drugRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, Drug.class, objectMapper);
            this.questRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, Quest.class, objectMapper);
            this.characterRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, GameCharacter.class, objectMapper);
            this.houseRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, House.class, objectMapper);
            this.garageRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, Garage.class, objectMapper);
            this.vehicleRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, Vehicle.class, objectMapper);
            this.characterDrugRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, CharacterDrug.class, objectMapper);
            this.characterQuestRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, CharacterQuest.class, objectMapper);
            this.gangRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, Gang.class, objectMapper);
            this.gangAffiliationRepository = new Neo4jEntityRepository<>(driver, sequenceRepository, GangAffiliation.class, objectMapper);
        }

        @Override
        public void clear() {
            try (org.neo4j.driver.Session session = driver.session()) {
                session.executeWrite(tx -> {
                    tx.run("MATCH (n) DETACH DELETE n");
                    return null;
                });
            }
        }

        @Override public EntityRepository<Profile> profileRepository() { return profileRepository; }
        @Override public EntityRepository<AuditLog> auditLogRepository() { return auditLogRepository; }
        @Override public EntityRepository<Drug> drugRepository() { return drugRepository; }
        @Override public EntityRepository<Quest> questRepository() { return questRepository; }
        @Override public EntityRepository<GameCharacter> characterRepository() { return characterRepository; }
        @Override public EntityRepository<House> houseRepository() { return houseRepository; }
        @Override public EntityRepository<Garage> garageRepository() { return garageRepository; }
        @Override public EntityRepository<Vehicle> vehicleRepository() { return vehicleRepository; }
        @Override public EntityRepository<CharacterDrug> characterDrugRepository() { return characterDrugRepository; }
        @Override public EntityRepository<CharacterQuest> characterQuestRepository() { return characterQuestRepository; }
        @Override public EntityRepository<Gang> gangRepository() { return gangRepository; }
        @Override public EntityRepository<GangAffiliation> gangAffiliationRepository() { return gangAffiliationRepository; }

        @Override
        public void close() {
            driver.close();
        }
    }
}
