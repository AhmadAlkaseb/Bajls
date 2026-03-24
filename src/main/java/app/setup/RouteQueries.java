package app.setup;

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

public final class RouteQueries {
    public static final ReadQuery<ProfileDTO> PROFILE_QUERY = new ReadQuery<>(
            "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role) FROM Profile p",
            "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role) FROM Profile p WHERE p.id = :id",
            ProfileDTO.class
    );

    public static final ReadQuery<AuditLogDTO> AUDIT_LOG_QUERY = new ReadQuery<>(
            "SELECT new app.dto.AuditLogDTO(a.id, a.actorProfileId, a.actorUsername, a.actorRole, a.action, a.entityName, a.entityId, a.requestMethod, a.requestPath, a.oldValues, a.newValues, a.changedAt) FROM AuditLog a ORDER BY a.changedAt DESC",
            "SELECT new app.dto.AuditLogDTO(a.id, a.actorProfileId, a.actorUsername, a.actorRole, a.action, a.entityName, a.entityId, a.requestMethod, a.requestPath, a.oldValues, a.newValues, a.changedAt) FROM AuditLog a WHERE a.id = :id",
            AuditLogDTO.class
    );

    public static final ReadQuery<DrugDTO> DRUG_QUERY = new ReadQuery<>(
            "SELECT new app.dto.DrugDTO(d.id, d.name, d.type) FROM Drug d",
            "SELECT new app.dto.DrugDTO(d.id, d.name, d.type) FROM Drug d WHERE d.id = :id",
            DrugDTO.class
    );

    public static final ReadQuery<QuestDTO> QUEST_QUERY = new ReadQuery<>(
            "SELECT new app.dto.QuestDTO(q.id, q.title, q.description, q.reward) FROM Quest q",
            "SELECT new app.dto.QuestDTO(q.id, q.title, q.description, q.reward) FROM Quest q WHERE q.id = :id",
            QuestDTO.class
    );

    public static final ReadQuery<GameCharacterDTO> CHARACTER_QUERY = new ReadQuery<>(
            "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender, c.skincolor, c.eyecolor, c.height, c.weight, c.house.id, c.garage.id) FROM GameCharacter c",
            "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender, c.skincolor, c.eyecolor, c.height, c.weight, c.house.id, c.garage.id) FROM GameCharacter c WHERE c.id = :id",
            GameCharacterDTO.class
    );

    public static final ReadQuery<HouseDTO> HOUSE_QUERY = new ReadQuery<>(
            "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, h.character.id) FROM House h",
            "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, h.character.id) FROM House h WHERE h.id = :id",
            HouseDTO.class
    );

    public static final ReadQuery<GarageDTO> GARAGE_QUERY = new ReadQuery<>(
            "SELECT new app.dto.GarageDTO(g.id, g.capacity, g.character.id) FROM Garage g",
            "SELECT new app.dto.GarageDTO(g.id, g.capacity, g.character.id) FROM Garage g WHERE g.id = :id",
            GarageDTO.class
    );

    public static final ReadQuery<VehicleDTO> VEHICLE_QUERY = new ReadQuery<>(
            "SELECT new app.dto.VehicleDTO(v.id, v.garage.id, v.model, v.type, v.plateNumber) FROM Vehicle v",
            "SELECT new app.dto.VehicleDTO(v.id, v.garage.id, v.model, v.type, v.plateNumber) FROM Vehicle v WHERE v.id = :id",
            VehicleDTO.class
    );

    public static final ReadQuery<CharacterDrugDTO> CHARACTER_DRUG_QUERY = new ReadQuery<>(
            "SELECT new app.dto.CharacterDrugDTO(cd.id, cd.character.id, cd.drug.id, cd.quantity) FROM CharacterDrug cd",
            "SELECT new app.dto.CharacterDrugDTO(cd.id, cd.character.id, cd.drug.id, cd.quantity) FROM CharacterDrug cd WHERE cd.id = :id",
            CharacterDrugDTO.class
    );

    public static final ReadQuery<CharacterQuestDTO> CHARACTER_QUEST_QUERY = new ReadQuery<>(
            "SELECT new app.dto.CharacterQuestDTO(cq.id, cq.character.id, cq.quest.id, cq.status, cq.acceptedAt) FROM CharacterQuest cq",
            "SELECT new app.dto.CharacterQuestDTO(cq.id, cq.character.id, cq.quest.id, cq.status, cq.acceptedAt) FROM CharacterQuest cq WHERE cq.id = :id",
            CharacterQuestDTO.class
    );

    public static final ReadQuery<GangDTO> GANG_QUERY = new ReadQuery<>(
            "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g",
            "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g WHERE g.id = :id",
            GangDTO.class
    );

    public static final ReadQuery<GangAffiliationDTO> GANG_AFFILIATION_QUERY = new ReadQuery<>(
            "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga",
            "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga WHERE ga.id = :id",
            GangAffiliationDTO.class
    );

    private RouteQueries() {
    }
}
