package app.setup;

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

public final class DtoMappers {

    private DtoMappers() {
    }

    public static ProfileDTO toProfileDto(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getUsername(),
                profile.getRole()
        );
    }

    public static DrugDTO toDrugDto(Drug drug) {
        return new DrugDTO(drug.getId(), drug.getName(), drug.getType());
    }

    public static QuestDTO toQuestDto(Quest quest) {
        return new QuestDTO(quest.getId(), quest.getTitle(), quest.getDescription(), quest.getReward());
    }

    public static GameCharacterDTO toGameCharacterDto(GameCharacter character) {
        return new GameCharacterDTO(
                character.getId(),
                character.getName(),
                character.getBalance(),
                nestedId(character.getProfile()),
                character.getGender(),
                character.getSkincolor(),
                character.getEyecolor(),
                character.getHeight(),
                character.getWeight(),
                nestedId(character.getHouse()),
                nestedId(character.getGarage())
        );
    }

    public static HouseDTO toHouseDto(House house) {
        return new HouseDTO(house.getId(), house.getAmountRooms(), house.getAmountBathrooms(), nestedId(house.getCharacter()));
    }

    public static GarageDTO toGarageDto(Garage garage) {
        return new GarageDTO(garage.getId(), garage.getCapacity(), nestedId(garage.getCharacter()));
    }

    public static VehicleDTO toVehicleDto(Vehicle vehicle) {
        return new VehicleDTO(vehicle.getId(), nestedId(vehicle.getGarage()), vehicle.getModel(), vehicle.getType(), vehicle.getPlateNumber());
    }

    public static CharacterDrugDTO toCharacterDrugDto(CharacterDrug characterDrug) {
        return new CharacterDrugDTO(
                characterDrug.getId(),
                nestedId(characterDrug.getCharacter()),
                nestedId(characterDrug.getDrug()),
                characterDrug.getQuantity()
        );
    }

    public static CharacterQuestDTO toCharacterQuestDto(CharacterQuest characterQuest) {
        return new CharacterQuestDTO(
                characterQuest.getId(),
                nestedId(characterQuest.getCharacter()),
                nestedId(characterQuest.getQuest()),
                characterQuest.getStatus(),
                characterQuest.getAcceptedAt()
        );
    }

    public static GangDTO toGangDto(Gang gang) {
        return new GangDTO(gang.getId(), gang.getName(), gang.getType());
    }

    public static GangAffiliationDTO toGangAffiliationDto(GangAffiliation gangAffiliation) {
        return new GangAffiliationDTO(
                gangAffiliation.getId(),
                nestedId(gangAffiliation.getCharacter()),
                nestedId(gangAffiliation.getGang()),
                gangAffiliation.getJoinDate()
        );
    }

    private static Long nestedId(Object entity) {
        return EntityIds.get(entity);
    }
}
