package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.ProfileDao;
import jakarta.persistence.EntityManagerFactory;
import persistence.HibernateConfig;
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

public final class JpaAppPersistence implements AppPersistence {
    private final EntityManagerFactory entityManagerFactory;
    private final AuthService authService;

    public JpaAppPersistence(boolean isTest) {
        this.entityManagerFactory = HibernateConfig.getEntityManagerFactoryConfig(isTest);
        this.authService = new AuthService(new ProfileDao(entityManagerFactory));
    }

    @Override
    public AuthService authService() {
        return authService;
    }

    @Override
    public CrudController<ProfileDTO, Profile> profileController() {
        return RouteControllers.profile(entityManagerFactory);
    }

    @Override
    public CrudController<DrugDTO, Drug> drugController() {
        return RouteControllers.drug(entityManagerFactory);
    }

    @Override
    public CrudController<QuestDTO, Quest> questController() {
        return RouteControllers.quest(entityManagerFactory);
    }

    @Override
    public CrudController<GameCharacterDTO, GameCharacter> characterController() {
        return RouteControllers.character(entityManagerFactory);
    }

    @Override
    public CrudController<HouseDTO, House> houseController() {
        return RouteControllers.house(entityManagerFactory);
    }

    @Override
    public CrudController<GarageDTO, Garage> garageController() {
        return RouteControllers.garage(entityManagerFactory);
    }

    @Override
    public CrudController<VehicleDTO, Vehicle> vehicleController() {
        return RouteControllers.vehicle(entityManagerFactory);
    }

    @Override
    public CrudController<CharacterDrugDTO, CharacterDrug> characterDrugController() {
        return RouteControllers.characterDrug(entityManagerFactory);
    }

    @Override
    public CrudController<CharacterQuestDTO, CharacterQuest> characterQuestController() {
        return RouteControllers.characterQuest(entityManagerFactory);
    }

    @Override
    public CrudController<GangDTO, Gang> gangController() {
        return RouteControllers.gang(entityManagerFactory);
    }

    @Override
    public CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliationController() {
        return RouteControllers.gangAffiliation(entityManagerFactory);
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }
}
