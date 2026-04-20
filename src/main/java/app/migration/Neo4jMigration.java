package app.migration;

import app.neo4j.Neo4jCharacterDrugRepository;
import app.neo4j.Neo4jCharacterQuestRepository;
import app.neo4j.Neo4jCharacterRepository;
import app.neo4j.Neo4jDisplayNames;
import app.neo4j.Neo4jEntityRepository;
import app.neo4j.Neo4jGarageRepository;
import app.neo4j.Neo4jGangAffiliationRepository;
import app.neo4j.Neo4jHouseRepository;
import app.neo4j.Neo4jProfileRepository;
import app.neo4j.Neo4jSequenceRepository;
import app.neo4j.Neo4jSupport;
import app.neo4j.Neo4jVehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import persistence.entity.Drug;
import persistence.entity.Gang;
import persistence.entity.Quest;

import java.util.Map;

public final class Neo4jMigration {
    private static final String DEFAULT_NEO4J_URI = "bolt://localhost:7687";
    private static final String DEFAULT_NEO4J_USER = "neo4j";
    private static final String DEFAULT_NEO4J_PASSWORD = "password";

    private Neo4jMigration() {
    }

    public static void write(MigrationSnapshot snapshot) {
        try (Driver driver = GraphDatabase.driver(uri(), AuthTokens.basic(user(), password()))) {
            driver.verifyConnectivity();
            clear(driver);
            saveSnapshot(snapshot, driver);
            Neo4jDisplayNames.apply(driver);
        }
    }

    private static void saveSnapshot(MigrationSnapshot snapshot, Driver driver) {
        ObjectMapper mapper = Neo4jSupport.createObjectMapper();
        Neo4jSequenceRepository sequences = new Neo4jSequenceRepository(driver);

        MigrationSupport.saveAll(new Neo4jProfileRepository(driver, sequences), snapshot.profiles());
        MigrationSupport.saveAll(new Neo4jEntityRepository<>(driver, sequences, Drug.class, mapper), snapshot.drugs());
        MigrationSupport.saveAll(new Neo4jEntityRepository<>(driver, sequences, Quest.class, mapper), snapshot.quests());
        MigrationSupport.saveAll(new Neo4jEntityRepository<>(driver, sequences, Gang.class, mapper), snapshot.gangs());
        MigrationSupport.saveAll(new Neo4jHouseRepository(driver, sequences), snapshot.houses());
        MigrationSupport.saveAll(new Neo4jGarageRepository(driver, sequences), snapshot.garages());
        MigrationSupport.saveAll(new Neo4jCharacterRepository(driver, sequences), snapshot.characters());
        MigrationSupport.saveAll(new Neo4jVehicleRepository(driver, sequences), snapshot.vehicles());
        MigrationSupport.saveAll(new Neo4jCharacterDrugRepository(driver, sequences), snapshot.characterDrugs());
        MigrationSupport.saveAll(new Neo4jCharacterQuestRepository(driver, sequences), snapshot.characterQuests());
        MigrationSupport.saveAll(new Neo4jGangAffiliationRepository(driver, sequences), snapshot.gangAffiliations());
    }

    private static void clear(Driver driver) {
        try (org.neo4j.driver.Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n", Map.of());
                return null;
            });
        }
    }

    private static String uri() {
        return System.getenv().getOrDefault("NEO4J_URI", DEFAULT_NEO4J_URI);
    }

    private static String user() {
        return System.getenv().getOrDefault("NEO4J_USER", DEFAULT_NEO4J_USER);
    }

    private static String password() {
        return System.getenv().getOrDefault("NEO4J_PASSWORD", DEFAULT_NEO4J_PASSWORD);
    }
}
