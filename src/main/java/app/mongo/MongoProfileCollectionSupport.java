package app.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
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
import persistence.enums.DrugType;
import persistence.enums.EyeColorType;
import persistence.enums.GangType;
import persistence.enums.GenderType;
import persistence.enums.ProfileRole;
import persistence.enums.QuestStatus;
import persistence.enums.SkinColorType;
import persistence.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MongoProfileCollectionSupport {
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoProfileCollectionSupport(MongoDatabase database) {
        this.database = database;
        this.collection = database.getCollection(MongoCollections.PROFILE);
    }

    public List<Profile> findAllProfiles() {
        List<Profile> profiles = new ArrayList<>();
        for (Document document : collection.find()) {
            profiles.add(toProfile(document));
        }
        return profiles;
    }

    public Profile findProfileById(Long id) {
        return toProfile(collection.find(Filters.eq("id", id)).first());
    }

    public Profile findProfileByUsername(String username) {
        return toProfile(collection.find(Filters.eq("username", username)).first());
    }

    public Profile saveProfile(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(MongoSupport.nextSequenceValue(database, MongoCollections.PROFILE));
        }

        hydrateProfile(profile);
        collection.replaceOne(
                Filters.eq("id", profile.getId()),
                toDocument(profile),
                new ReplaceOptions().upsert(true)
        );
        return profile;
    }

    public void deleteProfile(Long id) {
        collection.deleteOne(Filters.eq("id", id));
    }

    public Long nextId(String sequenceName) {
        return MongoSupport.nextSequenceValue(database, sequenceName);
    }

    public static void hydrateProfile(Profile profile) {
        if (profile == null) {
            return;
        }

        if (profile.getCharacters() == null) {
            profile.setCharacters(new ArrayList<>());
        }

        for (GameCharacter character : profile.getCharacters()) {
            hydrateCharacter(profile, character);
        }
    }

    private static void hydrateCharacter(Profile profile, GameCharacter character) {
        if (character == null) {
            return;
        }

        character.setProfile(profile);
        if (character.getCharacterDrugs() == null) {
            character.setCharacterDrugs(new ArrayList<>());
        }
        if (character.getCharacterQuests() == null) {
            character.setCharacterQuests(new ArrayList<>());
        }
        if (character.getGangAffiliations() == null) {
            character.setGangAffiliations(new ArrayList<>());
        }

        if (character.getHouse() != null) {
            character.getHouse().setCharacter(character);
        }

        if (character.getGarage() != null) {
            Garage garage = character.getGarage();
            garage.setCharacter(character);
            if (garage.getVehicles() == null) {
                garage.setVehicles(new ArrayList<>());
            }
            for (Vehicle vehicle : garage.getVehicles()) {
                vehicle.setGarage(garage);
            }
        }

        for (CharacterDrug characterDrug : character.getCharacterDrugs()) {
            characterDrug.setCharacter(character);
            if (characterDrug.getDrug() != null && characterDrug.getDrug().getCharacterDrugs() == null) {
                characterDrug.getDrug().setCharacterDrugs(new ArrayList<>());
            }
        }

        for (CharacterQuest characterQuest : character.getCharacterQuests()) {
            characterQuest.setCharacter(character);
            if (characterQuest.getQuest() != null && characterQuest.getQuest().getCharacterQuests() == null) {
                characterQuest.getQuest().setCharacterQuests(new ArrayList<>());
            }
        }

        for (GangAffiliation gangAffiliation : character.getGangAffiliations()) {
            gangAffiliation.setCharacter(character);
            if (gangAffiliation.getGang() != null && gangAffiliation.getGang().getAffiliations() == null) {
                gangAffiliation.getGang().setAffiliations(new ArrayList<>());
            }
        }
    }

    private static Document toDocument(Profile profile) {
        List<Document> characters = new ArrayList<>();
        for (GameCharacter character : safe(profile.getCharacters())) {
            characters.add(toCharacterDocument(character));
        }

        return new Document("id", profile.getId())
                .append("firstName", profile.getFirstName())
                .append("lastName", profile.getLastName())
                .append("email", profile.getEmail())
                .append("username", profile.getUsername())
                .append("password", profile.getPassword())
                .append("role", enumName(profile.getRole()))
                .append("characters", characters);
    }

    private static Document toCharacterDocument(GameCharacter character) {
        List<Document> vehicles = new ArrayList<>();
        if (character.getGarage() != null) {
            for (Vehicle vehicle : safe(character.getGarage().getVehicles())) {
                vehicles.add(new Document("id", vehicle.getId())
                        .append("model", vehicle.getModel())
                        .append("type", enumName(vehicle.getType()))
                        .append("plateNumber", vehicle.getPlateNumber()));
            }
        }

        List<Document> characterDrugs = new ArrayList<>();
        for (CharacterDrug characterDrug : safe(character.getCharacterDrugs())) {
            characterDrugs.add(new Document("id", characterDrug.getId())
                    .append("drugId", nestedId(characterDrug.getDrug()))
                    .append("quantity", characterDrug.getQuantity()));
        }

        List<Document> characterQuests = new ArrayList<>();
        for (CharacterQuest characterQuest : safe(character.getCharacterQuests())) {
            characterQuests.add(new Document("id", characterQuest.getId())
                    .append("questId", nestedId(characterQuest.getQuest()))
                    .append("status", enumName(characterQuest.getStatus()))
                    .append("acceptedAt", characterQuest.getAcceptedAt() == null ? null : characterQuest.getAcceptedAt().toString()));
        }

        List<Document> gangAffiliations = new ArrayList<>();
        for (GangAffiliation gangAffiliation : safe(character.getGangAffiliations())) {
            gangAffiliations.add(new Document("id", gangAffiliation.getId())
                    .append("gangId", nestedId(gangAffiliation.getGang()))
                    .append("joinDate", gangAffiliation.getJoinDate() == null ? null : gangAffiliation.getJoinDate().toString()));
        }

        Document house = null;
        if (character.getHouse() != null) {
            house = new Document("id", character.getHouse().getId())
                    .append("amountRooms", character.getHouse().getAmountRooms())
                    .append("amountBathrooms", character.getHouse().getAmountBathrooms());
        }

        Document garage = null;
        if (character.getGarage() != null) {
            garage = new Document("id", character.getGarage().getId())
                    .append("capacity", character.getGarage().getCapacity())
                    .append("vehicles", vehicles);
        }

        return new Document("id", character.getId())
                .append("name", character.getName())
                .append("balance", character.getBalance())
                .append("gender", enumName(character.getGender()))
                .append("skincolor", enumName(character.getSkincolor()))
                .append("eyecolor", enumName(character.getEyecolor()))
                .append("height", character.getHeight())
                .append("weight", character.getWeight())
                .append("house", house)
                .append("garage", garage)
                .append("characterDrugs", characterDrugs)
                .append("characterQuests", characterQuests)
                .append("gangAffiliations", gangAffiliations);
    }

    private static Profile toProfile(Document document) {
        if (document == null) {
            return null;
        }

        Profile profile = Profile.builder()
                .id(number(document.get("id")))
                .firstName(document.getString("firstName"))
                .lastName(document.getString("lastName"))
                .email(document.getString("email"))
                .username(document.getString("username"))
                .password(document.getString("password"))
                .role(enumValue(ProfileRole.class, document.getString("role")))
                .characters(new ArrayList<>())
                .build();

        for (Document characterDocument : documents(document.get("characters"))) {
            profile.getCharacters().add(toCharacter(profile, characterDocument));
        }
        return profile;
    }

    private static GameCharacter toCharacter(Profile profile, Document document) {
        House house = null;
        Document houseDocument = document.get("house", Document.class);
        if (houseDocument != null) {
            house = House.builder()
                    .id(number(houseDocument.get("id")))
                    .amountRooms(intValue(houseDocument.get("amountRooms")))
                    .amountBathrooms(intValue(houseDocument.get("amountBathrooms")))
                    .build();
        }

        Garage garage = null;
        Document garageDocument = document.get("garage", Document.class);
        if (garageDocument != null) {
            garage = Garage.builder()
                    .id(number(garageDocument.get("id")))
                    .capacity(intValue(garageDocument.get("capacity")))
                    .vehicles(new ArrayList<>())
                    .build();

            for (Document vehicleDocument : documents(garageDocument.get("vehicles"))) {
                garage.getVehicles().add(Vehicle.builder()
                        .id(number(vehicleDocument.get("id")))
                        .model(vehicleDocument.getString("model"))
                        .type(enumValue(VehicleType.class, vehicleDocument.getString("type")))
                        .plateNumber(vehicleDocument.getString("plateNumber"))
                        .build());
            }
        }

        GameCharacter character = GameCharacter.builder()
                .id(number(document.get("id")))
                .name(document.getString("name"))
                .balance(decimalValue(document.get("balance")))
                .profile(profile)
                .gender(enumValue(GenderType.class, document.getString("gender")))
                .skincolor(enumValue(SkinColorType.class, document.getString("skincolor")))
                .eyecolor(enumValue(EyeColorType.class, document.getString("eyecolor")))
                .height(document.getString("height"))
                .weight(document.getString("weight"))
                .house(house)
                .garage(garage)
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build();

        for (Document characterDrugDocument : documents(document.get("characterDrugs"))) {
            character.getCharacterDrugs().add(CharacterDrug.builder()
                    .id(number(characterDrugDocument.get("id")))
                    .character(character)
                    .drug(Drug.builder()
                            .id(number(characterDrugDocument.get("drugId")))
                            .type(enumValue(DrugType.class, null))
                            .characterDrugs(new ArrayList<>())
                            .build())
                    .quantity(intValue(characterDrugDocument.get("quantity")))
                    .build());
        }

        for (Document characterQuestDocument : documents(document.get("characterQuests"))) {
            character.getCharacterQuests().add(CharacterQuest.builder()
                    .id(number(characterQuestDocument.get("id")))
                    .character(character)
                    .quest(Quest.builder()
                            .id(number(characterQuestDocument.get("questId")))
                            .characterQuests(new ArrayList<>())
                            .build())
                    .status(enumValue(QuestStatus.class, characterQuestDocument.getString("status")))
                    .acceptedAt(localDateTime(characterQuestDocument.getString("acceptedAt")))
                    .build());
        }

        for (Document gangAffiliationDocument : documents(document.get("gangAffiliations"))) {
            character.getGangAffiliations().add(GangAffiliation.builder()
                    .id(number(gangAffiliationDocument.get("id")))
                    .character(character)
                    .gang(Gang.builder()
                            .id(number(gangAffiliationDocument.get("gangId")))
                            .type(enumValue(GangType.class, null))
                            .affiliations(new ArrayList<>())
                            .build())
                    .joinDate(localDate(gangAffiliationDocument.getString("joinDate")))
                    .build());
        }

        hydrateCharacter(profile, character);
        return character;
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static List<Document> documents(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Document> documents = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Document document) {
                documents.add(document);
            }
        }
        return documents;
    }

    private static Long nestedId(Object entity) {
        return entity == null ? null : app.audit.AuditSnapshotUtil.getEntityId(entity);
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    private static Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(Objects.toString(value));
    }

    private static LocalDateTime localDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private static LocalDate localDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
