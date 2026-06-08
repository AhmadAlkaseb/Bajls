package app.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MongoProfileCollectionSupport {
    private final MongoCollection<Document> collection;
    private final Map<String, Long> allocatedIds = new HashMap<>();

    public MongoProfileCollectionSupport(MongoDatabase database) {
        this.collection = database.getCollection(MongoCollections.PROFILE);
    }

    public List<Profile> findAllProfiles() {
        List<Profile> profiles = new ArrayList<>();
        for (Document document : collection.find()) {
            profiles.add(MongoProfileEntityMapper.toProfile(document));
        }
        return profiles;
    }

    public Profile findProfileById(Long id) {
        return MongoProfileEntityMapper.toProfile(collection.find(Filters.eq("id", id)).first());
    }

    public Profile findProfileByUsername(String username) {
        return MongoProfileEntityMapper.toProfile(collection.find(Filters.eq("username", username)).first());
    }

    public Profile saveProfile(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(nextId(MongoCollections.PROFILE));
        }

        MongoProfileHydrator.hydrateProfile(profile);
        collection.replaceOne(
                Filters.eq("id", profile.getId()),
                MongoProfileDocumentMapper.toDocument(profile),
                new ReplaceOptions().upsert(true)
        );
        return profile;
    }

    public void deleteProfile(Long id) {
        collection.deleteOne(Filters.eq("id", id));
    }

    public Long nextId(String sequenceName) {
        Long currentMax = Math.max(
                allocatedIds.getOrDefault(sequenceName, 0L),
                maxExistingId(sequenceName)
        );
        Long nextId = currentMax + 1;
        allocatedIds.put(sequenceName, nextId);
        return nextId;
    }

    private Long maxExistingId(String sequenceName) {
        long max = 0L;
        for (Profile profile : findAllProfiles()) {
            if (MongoCollections.PROFILE.equals(sequenceName)) {
                max = Math.max(max, idOrZero(profile.getId()));
                continue;
            }
            if (profile.getCharacters() == null) {
                continue;
            }
            for (persistence.entity.GameCharacter character : profile.getCharacters()) {
                max = Math.max(max, maxCharacterId(sequenceName, character));
            }
        }
        return max;
    }

    private long maxCharacterId(String sequenceName, persistence.entity.GameCharacter character) {
        return switch (sequenceName) {
            case "characters" -> idOrZero(character.getId());
            case "houses" -> character.getHouse() == null ? 0L : idOrZero(character.getHouse().getId());
            case "garages" -> character.getGarage() == null ? 0L : idOrZero(character.getGarage().getId());
            case "vehicles" -> maxVehicleId(character);
            case "character_drug" -> character.getCharacterDrugs() == null ? 0L : character.getCharacterDrugs().stream()
                    .mapToLong(row -> idOrZero(row.getId()))
                    .max()
                    .orElse(0L);
            case "character_quest" -> character.getCharacterQuests() == null ? 0L : character.getCharacterQuests().stream()
                    .mapToLong(row -> idOrZero(row.getId()))
                    .max()
                    .orElse(0L);
            case "gang_affiliations" -> character.getGangAffiliations() == null ? 0L : character.getGangAffiliations().stream()
                    .mapToLong(row -> idOrZero(row.getId()))
                    .max()
                    .orElse(0L);
            default -> 0L;
        };
    }

    private long maxVehicleId(persistence.entity.GameCharacter character) {
        if (character.getGarage() == null || character.getGarage().getVehicles() == null) {
            return 0L;
        }
        return character.getGarage().getVehicles().stream()
                .mapToLong(vehicle -> idOrZero(vehicle.getId()))
                .max()
                .orElse(0L);
    }

    private long idOrZero(Long id) {
        return id == null ? 0L : id;
    }
}
