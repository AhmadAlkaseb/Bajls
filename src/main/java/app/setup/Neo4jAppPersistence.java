package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.neo4j.Neo4jEntityRepository;
import app.neo4j.Neo4jProfileRepository;
import app.neo4j.Neo4jSequenceRepository;
import app.neo4j.Neo4jSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
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
import app.dto.AuditLogDTO;
import app.dto.CharacterDrugDTO;
import app.dto.CharacterQuestDTO;
import app.dto.DrugDTO;
import app.dto.GameCharacterDTO;
import app.dto.GangAffiliationDTO;
import app.dto.GangDTO;
import app.dto.GarageDTO;
import app.dto.HouseDTO;
import app.dto.ProfileDTO;
import app.dto.QuestDTO;
import app.dto.VehicleDTO;

public final class Neo4jAppPersistence implements AppPersistence {
    private static final String DEFAULT_NEO4J_URI = "bolt://localhost:7687";
    private static final String DEFAULT_NEO4J_USER = "neo4j";
    private static final String DEFAULT_NEO4J_PASSWORD = "password";

    private final Driver driver;
    private final AuthService authService;
    private final CrudController<ProfileDTO, Profile> profileController;
    private final CrudController<AuditLogDTO, AuditLog> auditLogController;
    private final CrudController<DrugDTO, Drug> drugController;
    private final CrudController<QuestDTO, Quest> questController;
    private final CrudController<GameCharacterDTO, GameCharacter> characterController;
    private final CrudController<HouseDTO, House> houseController;
    private final CrudController<GarageDTO, Garage> garageController;
    private final CrudController<VehicleDTO, Vehicle> vehicleController;
    private final CrudController<CharacterDrugDTO, CharacterDrug> characterDrugController;
    private final CrudController<CharacterQuestDTO, CharacterQuest> characterQuestController;
    private final CrudController<GangDTO, Gang> gangController;
    private final CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliationController;

    public Neo4jAppPersistence() {
        this.driver = GraphDatabase.driver(
                System.getenv().getOrDefault("NEO4J_URI", DEFAULT_NEO4J_URI),
                AuthTokens.basic(
                        System.getenv().getOrDefault("NEO4J_USER", DEFAULT_NEO4J_USER),
                        System.getenv().getOrDefault("NEO4J_PASSWORD", DEFAULT_NEO4J_PASSWORD)
                )
        );

        ObjectMapper objectMapper = Neo4jSupport.createObjectMapper();
        Neo4jSequenceRepository sequenceRepository = new Neo4jSequenceRepository(driver);
        BackendRepositories repositories = new BackendRepositories(
                new Neo4jProfileRepository(driver, sequenceRepository, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, AuditLog.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, Drug.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, Quest.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, GameCharacter.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, House.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, Garage.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, Vehicle.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, CharacterDrug.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, CharacterQuest.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, Gang.class, objectMapper),
                new Neo4jEntityRepository<>(driver, sequenceRepository, GangAffiliation.class, objectMapper)
        );
        BackendControllers controllers = PersistenceSupport.controllers(repositories);

        this.authService = controllers.authService();
        this.profileController = controllers.profileController();
        this.auditLogController = controllers.auditLogController();
        this.drugController = controllers.drugController();
        this.questController = controllers.questController();
        this.characterController = controllers.characterController();
        this.houseController = controllers.houseController();
        this.garageController = controllers.garageController();
        this.vehicleController = controllers.vehicleController();
        this.characterDrugController = controllers.characterDrugController();
        this.characterQuestController = controllers.characterQuestController();
        this.gangController = controllers.gangController();
        this.gangAffiliationController = controllers.gangAffiliationController();
    }

    @Override public AuthService authService() { return authService; }
    @Override public CrudController<ProfileDTO, Profile> profileController() { return profileController; }
    @Override public CrudController<AuditLogDTO, AuditLog> auditLogController() { return auditLogController; }
    @Override public CrudController<DrugDTO, Drug> drugController() { return drugController; }
    @Override public CrudController<QuestDTO, Quest> questController() { return questController; }
    @Override public CrudController<GameCharacterDTO, GameCharacter> characterController() { return characterController; }
    @Override public CrudController<HouseDTO, House> houseController() { return houseController; }
    @Override public CrudController<GarageDTO, Garage> garageController() { return garageController; }
    @Override public CrudController<VehicleDTO, Vehicle> vehicleController() { return vehicleController; }
    @Override public CrudController<CharacterDrugDTO, CharacterDrug> characterDrugController() { return characterDrugController; }
    @Override public CrudController<CharacterQuestDTO, CharacterQuest> characterQuestController() { return characterQuestController; }
    @Override public CrudController<GangDTO, Gang> gangController() { return gangController; }
    @Override public CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliationController() { return gangAffiliationController; }

    @Override
    public void close() {
        driver.close();
    }
}
