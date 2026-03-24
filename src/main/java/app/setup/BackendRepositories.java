package app.setup;

import app.dao.EntityRepository;
import app.dao.ProfileEntityRepository;
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

public record BackendRepositories(
        ProfileEntityRepository profileRepository,
        EntityRepository<AuditLog> auditLogRepository,
        EntityRepository<Drug> drugRepository,
        EntityRepository<Quest> questRepository,
        EntityRepository<GameCharacter> characterRepository,
        EntityRepository<House> houseRepository,
        EntityRepository<Garage> garageRepository,
        EntityRepository<Vehicle> vehicleRepository,
        EntityRepository<CharacterDrug> characterDrugRepository,
        EntityRepository<CharacterQuest> characterQuestRepository,
        EntityRepository<Gang> gangRepository,
        EntityRepository<GangAffiliation> gangAffiliationRepository
) {
}
