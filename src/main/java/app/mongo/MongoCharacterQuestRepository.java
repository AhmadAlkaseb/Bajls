package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.CharacterQuest;
import persistence.entity.GameCharacter;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public class MongoCharacterQuestRepository implements EntityRepository<CharacterQuest> {
    private final MongoProfileCollectionSupport support;

    public MongoCharacterQuestRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<CharacterQuest> findAll() {
        List<CharacterQuest> values = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                values.addAll(character.getCharacterQuests());
            }
        }
        return values;
    }

    @Override
    public CharacterQuest findById(Long id) {
        for (CharacterQuest value : findAll()) {
            if (id.equals(value.getId())) {
                return value;
            }
        }
        return null;
    }

    @Override
    public CharacterQuest save(CharacterQuest entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("character_quest"));
        }
        Long characterId = entity.getCharacter() == null ? null : entity.getCharacter().getId();

        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (characterId != null && characterId.equals(character.getId())) {
                    character.getCharacterQuests().removeIf(value -> entity.getId().equals(value.getId()));
                    character.getCharacterQuests().add(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("CharacterQuest must reference an existing character");
    }

    @Override
    public CharacterQuest update(CharacterQuest entity) {
        if (entity.getCharacter() == null || entity.getCharacter().getId() == null) {
            CharacterQuest existing = findById(entity.getId());
            if (existing != null) {
                entity.setCharacter(existing.getCharacter());
            }
        }
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (character.getCharacterQuests().removeIf(value -> id.equals(value.getId()))) {
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
