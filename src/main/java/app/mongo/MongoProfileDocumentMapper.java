package app.mongo;

import app.setup.EntityIds;
import org.bson.Document;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.GangAffiliation;
import persistence.entity.GameCharacter;
import persistence.entity.Profile;
import persistence.entity.Vehicle;

import java.util.ArrayList;
import java.util.List;

final class MongoProfileDocumentMapper {
    private MongoProfileDocumentMapper() {
    }

    static Document toDocument(Profile profile) {
        List<Document> characters = new ArrayList<>();
        for (GameCharacter character : MongoProfileValues.safe(profile.getCharacters())) {
            characters.add(toCharacterDocument(character));
        }

        return new Document("id", profile.getId())
                .append("firstName", profile.getFirstName())
                .append("lastName", profile.getLastName())
                .append("email", profile.getEmail())
                .append("username", profile.getUsername())
                .append("password", profile.getPassword())
                .append("role", MongoProfileValues.enumName(profile.getRole()))
                .append("characters", characters);
    }

    private static Document toCharacterDocument(GameCharacter character) {
        return new Document("id", character.getId())
                .append("name", character.getName())
                .append("balance", character.getBalance())
                .append("gender", MongoProfileValues.enumName(character.getGender()))
                .append("skincolor", MongoProfileValues.enumName(character.getSkincolor()))
                .append("eyecolor", MongoProfileValues.enumName(character.getEyecolor()))
                .append("height", character.getHeight())
                .append("weight", character.getWeight())
                .append("house", houseDocument(character))
                .append("garage", garageDocument(character))
                .append("characterDrugs", characterDrugs(character))
                .append("characterQuests", characterQuests(character))
                .append("gangAffiliations", gangAffiliations(character));
    }

    private static Document houseDocument(GameCharacter character) {
        if (character.getHouse() == null) {
            return null;
        }
        return new Document("id", character.getHouse().getId())
                .append("amountRooms", character.getHouse().getAmountRooms())
                .append("amountBathrooms", character.getHouse().getAmountBathrooms());
    }

    private static Document garageDocument(GameCharacter character) {
        if (character.getGarage() == null) {
            return null;
        }

        List<Document> vehicles = new ArrayList<>();
        for (Vehicle vehicle : MongoProfileValues.safe(character.getGarage().getVehicles())) {
            vehicles.add(new Document("id", vehicle.getId())
                    .append("model", vehicle.getModel())
                    .append("type", MongoProfileValues.enumName(vehicle.getType()))
                    .append("plateNumber", vehicle.getPlateNumber()));
        }

        return new Document("id", character.getGarage().getId())
                .append("capacity", character.getGarage().getCapacity())
                .append("vehicles", vehicles);
    }

    private static List<Document> characterDrugs(GameCharacter character) {
        List<Document> documents = new ArrayList<>();
        for (CharacterDrug characterDrug : MongoProfileValues.safe(character.getCharacterDrugs())) {
            documents.add(new Document("id", characterDrug.getId())
                    .append("drugId", EntityIds.get(characterDrug.getDrug()))
                    .append("quantity", characterDrug.getQuantity()));
        }
        return documents;
    }

    private static List<Document> characterQuests(GameCharacter character) {
        List<Document> documents = new ArrayList<>();
        for (CharacterQuest characterQuest : MongoProfileValues.safe(character.getCharacterQuests())) {
            documents.add(new Document("id", characterQuest.getId())
                    .append("questId", EntityIds.get(characterQuest.getQuest()))
                    .append("status", MongoProfileValues.enumName(characterQuest.getStatus()))
                    .append("acceptedAt", MongoProfileValues.stringValue(characterQuest.getAcceptedAt())));
        }
        return documents;
    }

    private static List<Document> gangAffiliations(GameCharacter character) {
        List<Document> documents = new ArrayList<>();
        for (GangAffiliation affiliation : MongoProfileValues.safe(character.getGangAffiliations())) {
            documents.add(new Document("id", affiliation.getId())
                    .append("gangId", EntityIds.get(affiliation.getGang()))
                    .append("joinDate", MongoProfileValues.stringValue(affiliation.getJoinDate())));
        }
        return documents;
    }
}
