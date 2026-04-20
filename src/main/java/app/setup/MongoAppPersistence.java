package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.mongo.MongoCharacterDrugRepository;
import app.mongo.MongoCharacterQuestRepository;
import app.mongo.MongoCharacterRepository;
import app.mongo.MongoCollections;
import app.mongo.MongoEntityRepository;
import app.mongo.MongoGarageRepository;
import app.mongo.MongoGangAffiliationRepository;
import app.mongo.MongoHouseRepository;
import app.mongo.MongoProfileRepository;
import app.mongo.MongoVehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
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
        MongoProfileRepository profiles = new MongoProfileRepository(database, objectMapper);

        this.authService = new AuthService(profiles);
        this.profileController = PersistenceSupport.profileController(profiles);
        this.drugController = PersistenceSupport.mappedCrudController(
                new MongoEntityRepository<>(database, MongoCollections.DRUGS, Drug.class, objectMapper),
                DtoMappers::toDrugDto,
                Drug.class
        );
        this.questController = PersistenceSupport.mappedCrudController(
                new MongoEntityRepository<>(database, MongoCollections.QUESTS, Quest.class, objectMapper),
                DtoMappers::toQuestDto,
                Quest.class
        );
        this.characterController = PersistenceSupport.mappedCrudController(new MongoCharacterRepository(database), DtoMappers::toGameCharacterDto, GameCharacter.class);
        this.houseController = PersistenceSupport.mappedCrudController(new MongoHouseRepository(database), DtoMappers::toHouseDto, House.class);
        this.garageController = PersistenceSupport.mappedCrudController(new MongoGarageRepository(database), DtoMappers::toGarageDto, Garage.class);
        this.vehicleController = PersistenceSupport.mappedCrudController(new MongoVehicleRepository(database), DtoMappers::toVehicleDto, Vehicle.class);
        this.characterDrugController = PersistenceSupport.mappedCrudController(new MongoCharacterDrugRepository(database), DtoMappers::toCharacterDrugDto, CharacterDrug.class);
        this.characterQuestController = PersistenceSupport.mappedCrudController(new MongoCharacterQuestRepository(database), DtoMappers::toCharacterQuestDto, CharacterQuest.class);
        this.gangController = PersistenceSupport.mappedCrudController(
                new MongoEntityRepository<>(database, MongoCollections.GANGS, Gang.class, objectMapper),
                DtoMappers::toGangDto,
                Gang.class
        );
        this.gangAffiliationController = PersistenceSupport.mappedCrudController(new MongoGangAffiliationRepository(database), DtoMappers::toGangAffiliationDto, GangAffiliation.class);
    }

    @Override public AuthService authService() { return authService; }
    @Override public CrudController<ProfileDTO, Profile> profileController() { return profileController; }
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
