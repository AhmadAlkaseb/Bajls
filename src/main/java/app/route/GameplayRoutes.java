package app.route;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.CharacterDrugDao;
import app.dao.CharacterQuestDao;
import app.dao.GameCharacterDao;
import app.dao.GangAffiliationDao;
import app.dao.GangDao;
import app.dao.GarageDao;
import app.dao.HouseDao;
import app.dao.JpaReadDao;
import app.dao.VehicleDao;
import app.dto.CharacterDrugDTO;
import app.dto.CharacterQuestDTO;
import app.dto.GameCharacterDTO;
import app.dto.GangAffiliationDTO;
import app.dto.GangDTO;
import app.dto.GarageDTO;
import app.dto.HouseDTO;
import app.dto.VehicleDTO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.Garage;
import persistence.entity.House;
import persistence.entity.Vehicle;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class GameplayRoutes {

    private GameplayRoutes() {
    }

    public static EndpointGroup routes(EntityManagerFactory entityManagerFactory, AuthService authService) {
        CrudController<GameCharacterDTO, GameCharacter> characterController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender, c.skincolor, c.eyecolor, c.height, c.weight, c.house.id, c.garage.id) FROM GameCharacter c",
                        "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender, c.skincolor, c.eyecolor, c.height, c.weight, c.house.id, c.garage.id) FROM GameCharacter c WHERE c.id = :id",
                        GameCharacterDTO.class
                ),
                new GameCharacterDao(entityManagerFactory),
                GameCharacter.class
        );

        CrudController<HouseDTO, House> houseController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, h.character.id) FROM House h",
                        "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, h.character.id) FROM House h WHERE h.id = :id",
                        HouseDTO.class
                ),
                new HouseDao(entityManagerFactory),
                House.class
        );

        CrudController<GarageDTO, Garage> garageController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.GarageDTO(g.id, g.capacity, g.character.id) FROM Garage g",
                        "SELECT new app.dto.GarageDTO(g.id, g.capacity, g.character.id) FROM Garage g WHERE g.id = :id",
                        GarageDTO.class
                ),
                new GarageDao(entityManagerFactory),
                Garage.class
        );

        CrudController<VehicleDTO, Vehicle> vehicleController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.VehicleDTO(v.id, v.garage.id, v.model, v.type, v.plateNumber) FROM Vehicle v",
                        "SELECT new app.dto.VehicleDTO(v.id, v.garage.id, v.model, v.type, v.plateNumber) FROM Vehicle v WHERE v.id = :id",
                        VehicleDTO.class
                ),
                new VehicleDao(entityManagerFactory),
                Vehicle.class
        );

        CrudController<CharacterDrugDTO, CharacterDrug> characterDrugController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.CharacterDrugDTO(cd.id, cd.character.id, cd.drug.id, cd.quantity) FROM CharacterDrug cd",
                        "SELECT new app.dto.CharacterDrugDTO(cd.id, cd.character.id, cd.drug.id, cd.quantity) FROM CharacterDrug cd WHERE cd.id = :id",
                        CharacterDrugDTO.class
                ),
                new CharacterDrugDao(entityManagerFactory),
                CharacterDrug.class
        );

        CrudController<CharacterQuestDTO, CharacterQuest> characterQuestController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.CharacterQuestDTO(cq.id, cq.character.id, cq.quest.id, cq.status, cq.acceptedAt) FROM CharacterQuest cq",
                        "SELECT new app.dto.CharacterQuestDTO(cq.id, cq.character.id, cq.quest.id, cq.status, cq.acceptedAt) FROM CharacterQuest cq WHERE cq.id = :id",
                        CharacterQuestDTO.class
                ),
                new CharacterQuestDao(entityManagerFactory),
                CharacterQuest.class
        );

        CrudController<GangDTO, Gang> gangController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g",
                        "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g WHERE g.id = :id",
                        GangDTO.class
                ),
                new GangDao(entityManagerFactory),
                Gang.class
        );

        CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliationController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga",
                        "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga WHERE ga.id = :id",
                        GangAffiliationDTO.class
                ),
                new GangAffiliationDao(entityManagerFactory),
                GangAffiliation.class
        );

        return () -> {
            path("characters", () -> {
                before(authService::requireAuthenticated);
                get(characterController::getAll);
                get("{id}", characterController::getById);
                post(characterController::create);
                put("{id}", characterController::update);
                delete("{id}", characterController::delete);
            });

            path("houses", () -> {
                before(authService::requireAuthenticated);
                get(houseController::getAll);
                get("{id}", houseController::getById);
                post(houseController::create);
                put("{id}", houseController::update);
                delete("{id}", houseController::delete);
            });

            path("garages", () -> {
                before(authService::requireAuthenticated);
                get(garageController::getAll);
                get("{id}", garageController::getById);
                post(garageController::create);
                put("{id}", garageController::update);
                delete("{id}", garageController::delete);
            });

            path("vehicles", () -> {
                before(authService::requireAuthenticated);
                get(vehicleController::getAll);
                get("{id}", vehicleController::getById);
                post(vehicleController::create);
                put("{id}", vehicleController::update);
                delete("{id}", vehicleController::delete);
            });

            path("character-drug", () -> {
                before(authService::requireAuthenticated);
                get(characterDrugController::getAll);
                get("{id}", characterDrugController::getById);
                post(characterDrugController::create);
                put("{id}", characterDrugController::update);
                delete("{id}", characterDrugController::delete);
            });

            path("character-quest", () -> {
                before(authService::requireAuthenticated);
                get(characterQuestController::getAll);
                get("{id}", characterQuestController::getById);
                post(characterQuestController::create);
                put("{id}", characterQuestController::update);
                delete("{id}", characterQuestController::delete);
            });

            path("gangs", () -> {
                before(authService::requireAuthenticated);
                get(gangController::getAll);
                get("{id}", gangController::getById);
                post(gangController::create);
                put("{id}", gangController::update);
                delete("{id}", gangController::delete);
            });

            path("gang-affiliations", () -> {
                before(authService::requireAuthenticated);
                get(gangAffiliationController::getAll);
                get("{id}", gangAffiliationController::getById);
                post(gangAffiliationController::create);
                put("{id}", gangAffiliationController::update);
                delete("{id}", gangAffiliationController::delete);
            });
        };
    }
}
