package app.mongo;

import org.bson.Document;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.Drug;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;
import persistence.entity.House;
import persistence.entity.Profile;
import persistence.entity.Quest;
import persistence.entity.Vehicle;
import persistence.enums.DrugType;
import persistence.enums.EyeColorType;
import persistence.enums.GangType;
import persistence.enums.GenderType;
import persistence.enums.ProfileRole;
import persistence.enums.QuestStatus;
import persistence.enums.SkinColorType;
import persistence.enums.VehicleType;

import java.util.ArrayList;

final class MongoProfileEntityMapper {
    private MongoProfileEntityMapper() {
    }

    static Profile toProfile(Document document) {
        if (document == null) {
            return null;
        }

        Profile profile = Profile.builder()
                .id(MongoProfileValues.number(document.get("id")))
                .firstName(document.getString("firstName"))
                .lastName(document.getString("lastName"))
                .email(document.getString("email"))
                .username(document.getString("username"))
                .password(document.getString("password"))
                .role(MongoProfileValues.enumValue(ProfileRole.class, document.getString("role")))
                .characters(new ArrayList<>())
                .build();

        for (Document characterDocument : MongoProfileValues.documents(document.get("characters"))) {
            profile.getCharacters().add(toCharacter(profile, characterDocument));
        }
        return profile;
    }

    private static GameCharacter toCharacter(Profile profile, Document document) {
        GameCharacter character = GameCharacter.builder()
                .id(MongoProfileValues.number(document.get("id")))
                .name(document.getString("name"))
                .balance(MongoProfileValues.decimalValue(document.get("balance")))
                .profile(profile)
                .gender(MongoProfileValues.enumValue(GenderType.class, document.getString("gender")))
                .skincolor(MongoProfileValues.enumValue(SkinColorType.class, document.getString("skincolor")))
                .eyecolor(MongoProfileValues.enumValue(EyeColorType.class, document.getString("eyecolor")))
                .height(document.getString("height"))
                .weight(document.getString("weight"))
                .house(toHouse(document.get("house", Document.class)))
                .garage(toGarage(document.get("garage", Document.class)))
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build();

        addRelations(character, document);
        MongoProfileHydrator.hydrateCharacter(profile, character);
        return character;
    }

    private static House toHouse(Document document) {
        if (document == null) {
            return null;
        }
        return House.builder()
                .id(MongoProfileValues.number(document.get("id")))
                .amountRooms(MongoProfileValues.intValue(document.get("amountRooms")))
                .amountBathrooms(MongoProfileValues.intValue(document.get("amountBathrooms")))
                .build();
    }

    private static Garage toGarage(Document document) {
        if (document == null) {
            return null;
        }

        Garage garage = Garage.builder()
                .id(MongoProfileValues.number(document.get("id")))
                .capacity(MongoProfileValues.intValue(document.get("capacity")))
                .vehicles(new ArrayList<>())
                .build();

        for (Document vehicleDocument : MongoProfileValues.documents(document.get("vehicles"))) {
            garage.getVehicles().add(Vehicle.builder()
                    .id(MongoProfileValues.number(vehicleDocument.get("id")))
                    .model(vehicleDocument.getString("model"))
                    .type(MongoProfileValues.enumValue(VehicleType.class, vehicleDocument.getString("type")))
                    .plateNumber(vehicleDocument.getString("plateNumber"))
                    .build());
        }
        return garage;
    }

    private static void addRelations(GameCharacter character, Document document) {
        for (Document item : MongoProfileValues.documents(document.get("characterDrugs"))) {
            character.getCharacterDrugs().add(CharacterDrug.builder()
                    .id(MongoProfileValues.number(item.get("id")))
                    .character(character)
                    .drug(Drug.builder().id(MongoProfileValues.number(item.get("drugId")))
                            .type(MongoProfileValues.enumValue(DrugType.class, null))
                            .characterDrugs(new ArrayList<>()).build())
                    .quantity(MongoProfileValues.intValue(item.get("quantity")))
                    .build());
        }

        for (Document item : MongoProfileValues.documents(document.get("characterQuests"))) {
            character.getCharacterQuests().add(CharacterQuest.builder()
                    .id(MongoProfileValues.number(item.get("id")))
                    .character(character)
                    .quest(Quest.builder().id(MongoProfileValues.number(item.get("questId")))
                            .characterQuests(new ArrayList<>()).build())
                    .status(MongoProfileValues.enumValue(QuestStatus.class, item.getString("status")))
                    .acceptedAt(MongoProfileValues.localDateTime(item.getString("acceptedAt")))
                    .build());
        }

        for (Document item : MongoProfileValues.documents(document.get("gangAffiliations"))) {
            character.getGangAffiliations().add(GangAffiliation.builder()
                    .id(MongoProfileValues.number(item.get("id")))
                    .character(character)
                    .gang(Gang.builder().id(MongoProfileValues.number(item.get("gangId")))
                            .type(MongoProfileValues.enumValue(GangType.class, null))
                            .affiliations(new ArrayList<>()).build())
                    .joinDate(MongoProfileValues.localDate(item.getString("joinDate")))
                    .build());
        }
    }
}
