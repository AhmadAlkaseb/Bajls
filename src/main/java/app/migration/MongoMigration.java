package app.migration;

import app.dao.EntityRepository;
import app.mongo.MongoCollections;
import app.mongo.MongoEntityRepository;
import app.mongo.MongoProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MongoMigration {
    private static final String DEFAULT_MONGO_URL = "mongodb://localhost:27017";
    private static final String DEFAULT_MONGO_DATABASE = "bajls";

    private MongoMigration() {
    }

    public static void write(MigrationSnapshot snapshot) {
        try (MongoClient client = MongoClients.create(System.getenv().getOrDefault("MONGO_URL", DEFAULT_MONGO_URL))) {
            MongoDatabase database = client.getDatabase(System.getenv().getOrDefault("MONGO_DB_NAME", DEFAULT_MONGO_DATABASE));
            database.drop();

            ObjectMapper mapper = app.mongo.MongoSupport.createObjectMapper();
            saveCatalog(snapshot, database, mapper);
            MigrationSupport.saveAll(new MongoProfileRepository(database, mapper), embeddedProfiles(snapshot));
        }
    }

    private static void saveCatalog(MigrationSnapshot snapshot, MongoDatabase database, ObjectMapper mapper) {
        EntityRepository<Drug> drugs = new MongoEntityRepository<>(database, MongoCollections.DRUGS, Drug.class, mapper);
        EntityRepository<Quest> quests = new MongoEntityRepository<>(database, MongoCollections.QUESTS, Quest.class, mapper);
        EntityRepository<Gang> gangs = new MongoEntityRepository<>(database, MongoCollections.GANGS, Gang.class, mapper);
        MigrationSupport.saveAll(drugs, snapshot.drugs());
        MigrationSupport.saveAll(quests, snapshot.quests());
        MigrationSupport.saveAll(gangs, snapshot.gangs());
    }

    private static List<Profile> embeddedProfiles(MigrationSnapshot snapshot) {
        Map<Long, Profile> profiles = snapshot.profiles().stream()
                .collect(Collectors.toMap(Profile::getId, profile -> profile));
        Map<Long, GameCharacter> characters = snapshot.characters().stream()
                .collect(Collectors.toMap(GameCharacter::getId, character -> character));
        Map<Long, House> houses = snapshot.houses().stream()
                .collect(Collectors.toMap(house -> house.getCharacter().getId(), house -> house));
        Map<Long, Garage> garages = snapshot.garages().stream()
                .collect(Collectors.toMap(garage -> garage.getCharacter().getId(), garage -> garage));

        profiles.values().forEach(profile -> profile.getCharacters().clear());
        attachCharacters(snapshot, profiles, characters, houses, garages);
        attachRelations(snapshot, characters);
        return profiles.values().stream().toList();
    }

    private static void attachCharacters(
            MigrationSnapshot snapshot,
            Map<Long, Profile> profiles,
            Map<Long, GameCharacter> characters,
            Map<Long, House> houses,
            Map<Long, Garage> garages
    ) {
        for (GameCharacter character : snapshot.characters()) {
            character.setHouse(houses.get(character.getId()));
            character.setGarage(garages.get(character.getId()));
            character.getCharacterDrugs().clear();
            character.getCharacterQuests().clear();
            character.getGangAffiliations().clear();
            if (character.getGarage() != null) {
                character.getGarage().setVehicles(new ArrayList<>());
            }

            Profile profile = profiles.get(character.getProfile().getId());
            if (profile != null) {
                profile.getCharacters().add(character);
            }
        }

        snapshot.vehicles().forEach(vehicle -> characters.values().stream()
                .filter(character -> hasGarage(character, vehicle.getGarage().getId()))
                .findFirst()
                .ifPresent(character -> character.getGarage().getVehicles().add(vehicle)));
    }

    private static void attachRelations(MigrationSnapshot snapshot, Map<Long, GameCharacter> characters) {
        for (CharacterDrug row : snapshot.characterDrugs()) {
            GameCharacter character = characters.get(row.getCharacter().getId());
            if (character != null) character.getCharacterDrugs().add(row);
        }
        for (CharacterQuest row : snapshot.characterQuests()) {
            GameCharacter character = characters.get(row.getCharacter().getId());
            if (character != null) character.getCharacterQuests().add(row);
        }
        for (GangAffiliation row : snapshot.gangAffiliations()) {
            GameCharacter character = characters.get(row.getCharacter().getId());
            if (character != null) character.getGangAffiliations().add(row);
        }
    }

    private static boolean hasGarage(GameCharacter character, Long garageId) {
        return character.getGarage() != null && character.getGarage().getId().equals(garageId);
    }
}
