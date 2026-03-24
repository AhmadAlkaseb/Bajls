package app.setup;

import app.controller.CrudController;
import app.dao.JpaDao;
import app.dao.ProfileDao;
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
import jakarta.persistence.EntityManagerFactory;
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

public final class RouteControllers {

    private RouteControllers() {
    }

    public static CrudController<ProfileDTO, Profile> profile(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.PROFILE_QUERY,
                new ProfileDao(entityManagerFactory),
                Profile.class
        );
    }

    public static CrudController<AuditLogDTO, AuditLog> auditLog(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.AUDIT_LOG_QUERY,
                new JpaDao<>(entityManagerFactory, AuditLog.class),
                AuditLog.class
        );
    }

    public static CrudController<DrugDTO, Drug> drug(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.DRUG_QUERY,
                new JpaDao<>(entityManagerFactory, Drug.class),
                Drug.class
        );
    }

    public static CrudController<QuestDTO, Quest> quest(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.QUEST_QUERY,
                new JpaDao<>(entityManagerFactory, Quest.class),
                Quest.class
        );
    }

    public static CrudController<GameCharacterDTO, GameCharacter> character(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.CHARACTER_QUERY,
                new JpaDao<>(entityManagerFactory, GameCharacter.class),
                GameCharacter.class
        );
    }

    public static CrudController<HouseDTO, House> house(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.HOUSE_QUERY,
                new JpaDao<>(entityManagerFactory, House.class),
                House.class
        );
    }

    public static CrudController<GarageDTO, Garage> garage(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.GARAGE_QUERY,
                new JpaDao<>(entityManagerFactory, Garage.class),
                Garage.class
        );
    }

    public static CrudController<VehicleDTO, Vehicle> vehicle(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.VEHICLE_QUERY,
                new JpaDao<>(entityManagerFactory, Vehicle.class),
                Vehicle.class
        );
    }

    public static CrudController<CharacterDrugDTO, CharacterDrug> characterDrug(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.CHARACTER_DRUG_QUERY,
                new JpaDao<>(entityManagerFactory, CharacterDrug.class),
                CharacterDrug.class
        );
    }

    public static CrudController<CharacterQuestDTO, CharacterQuest> characterQuest(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.CHARACTER_QUEST_QUERY,
                new JpaDao<>(entityManagerFactory, CharacterQuest.class),
                CharacterQuest.class
        );
    }

    public static CrudController<GangDTO, Gang> gang(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.GANG_QUERY,
                new JpaDao<>(entityManagerFactory, Gang.class),
                Gang.class
        );
    }

    public static CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliation(EntityManagerFactory entityManagerFactory) {
        return RouteSupport.jpaCrudController(
                entityManagerFactory,
                RouteQueries.GANG_AFFILIATION_QUERY,
                new JpaDao<>(entityManagerFactory, GangAffiliation.class),
                GangAffiliation.class
        );
    }
}
