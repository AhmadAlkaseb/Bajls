package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.mongo.MongoEntityRepository;
import app.mongo.MongoProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
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

public final class MongoAppPersistence implements AppPersistence {
    private static final String DEFAULT_MONGO_URL = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE_NAME = "bajls";

    private final MongoClient mongoClient;
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

    public MongoAppPersistence() {
        this.mongoClient = MongoClients.create(System.getenv().getOrDefault("MONGO_URL", DEFAULT_MONGO_URL));
        MongoDatabase database = mongoClient.getDatabase(System.getenv().getOrDefault("MONGO_DB_NAME", DEFAULT_DATABASE_NAME));
        ObjectMapper objectMapper = app.mongo.MongoSupport.createObjectMapper();
        BackendRepositories repositories = new BackendRepositories(
                new MongoProfileRepository(database, objectMapper),
                new MongoEntityRepository<>(database, "audit_log", AuditLog.class, objectMapper),
                new MongoEntityRepository<>(database, "drugs", Drug.class, objectMapper),
                new MongoEntityRepository<>(database, "quests", Quest.class, objectMapper),
                new MongoEntityRepository<>(database, "characters", GameCharacter.class, objectMapper),
                new MongoEntityRepository<>(database, "houses", House.class, objectMapper),
                new MongoEntityRepository<>(database, "garages", Garage.class, objectMapper),
                new MongoEntityRepository<>(database, "vehicles", Vehicle.class, objectMapper),
                new MongoEntityRepository<>(database, "character_drug", CharacterDrug.class, objectMapper),
                new MongoEntityRepository<>(database, "character_quest", CharacterQuest.class, objectMapper),
                new MongoEntityRepository<>(database, "gangs", Gang.class, objectMapper),
                new MongoEntityRepository<>(database, "gang_affiliations", GangAffiliation.class, objectMapper)
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
        mongoClient.close();
    }
}
