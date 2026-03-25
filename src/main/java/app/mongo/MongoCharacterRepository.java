package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.GameCharacter;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public class MongoCharacterRepository implements EntityRepository<GameCharacter> {
    private final MongoProfileCollectionSupport support;

    public MongoCharacterRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<GameCharacter> findAll() {
        List<GameCharacter> characters = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            characters.addAll(profile.getCharacters());
        }
        return characters;
    }

    @Override
    public GameCharacter findById(Long id) {
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (id.equals(character.getId())) {
                    return character;
                }
            }
        }
        return null;
    }

    @Override
    public GameCharacter save(GameCharacter entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("characters"));
        }
        assignNestedIds(entity);

        Profile profile = support.findProfileById(entity.getProfile() == null ? null : entity.getProfile().getId());
        if (profile == null) {
            throw new IllegalArgumentException("Character must reference an existing profile");
        }

        profile.getCharacters().removeIf(character -> entity.getId().equals(character.getId()));
        profile.getCharacters().add(entity);
        support.saveProfile(profile);
        return findById(entity.getId());
    }

    @Override
    public GameCharacter update(GameCharacter entity) {
        GameCharacter existing = findById(entity.getId());
        if (existing != null && (entity.getProfile() == null || entity.getProfile().getId() == null)) {
            entity.setProfile(existing.getProfile());
        }
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        for (Profile profile : support.findAllProfiles()) {
            boolean removed = profile.getCharacters().removeIf(character -> id.equals(character.getId()));
            if (removed) {
                support.saveProfile(profile);
                return;
            }
        }
    }

    private void assignNestedIds(GameCharacter character) {
        if (character.getCharacterDrugs() == null) {
            character.setCharacterDrugs(new ArrayList<>());
        }
        if (character.getCharacterQuests() == null) {
            character.setCharacterQuests(new ArrayList<>());
        }
        if (character.getGangAffiliations() == null) {
            character.setGangAffiliations(new ArrayList<>());
        }

        if (character.getHouse() != null && character.getHouse().getId() == null) {
            character.getHouse().setId(support.nextId("houses"));
        }
        if (character.getGarage() != null) {
            if (character.getGarage().getId() == null) {
                character.getGarage().setId(support.nextId("garages"));
            }
            if (character.getGarage().getVehicles() == null) {
                character.getGarage().setVehicles(new ArrayList<>());
            }
            character.getGarage().getVehicles().forEach(vehicle -> {
                if (vehicle.getId() == null) {
                    vehicle.setId(support.nextId("vehicles"));
                }
            });
        }
        character.getCharacterDrugs().forEach(characterDrug -> {
            if (characterDrug.getId() == null) {
                characterDrug.setId(support.nextId("character_drug"));
            }
        });
        character.getCharacterQuests().forEach(characterQuest -> {
            if (characterQuest.getId() == null) {
                characterQuest.setId(support.nextId("character_quest"));
            }
        });
        character.getGangAffiliations().forEach(gangAffiliation -> {
            if (gangAffiliation.getId() == null) {
                gangAffiliation.setId(support.nextId("gang_affiliations"));
            }
        });
    }
}
