package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
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

public record BackendControllers(
        AuthService authService,
        CrudController<ProfileDTO, Profile> profileController,
        CrudController<AuditLogDTO, AuditLog> auditLogController,
        CrudController<DrugDTO, Drug> drugController,
        CrudController<QuestDTO, Quest> questController,
        CrudController<GameCharacterDTO, GameCharacter> characterController,
        CrudController<HouseDTO, House> houseController,
        CrudController<GarageDTO, Garage> garageController,
        CrudController<VehicleDTO, Vehicle> vehicleController,
        CrudController<CharacterDrugDTO, CharacterDrug> characterDrugController,
        CrudController<CharacterQuestDTO, CharacterQuest> characterQuestController,
        CrudController<GangDTO, Gang> gangController,
        CrudController<GangAffiliationDTO, GangAffiliation> gangAffiliationController
) {
}
