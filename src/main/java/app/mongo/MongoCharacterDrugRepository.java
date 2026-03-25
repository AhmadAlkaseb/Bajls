package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.CharacterDrug;
import persistence.entity.GameCharacter;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public class MongoCharacterDrugRepository implements EntityRepository<CharacterDrug> {
    private final MongoProfileCollectionSupport support;

    public MongoCharacterDrugRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<CharacterDrug> findAll() {
        List<CharacterDrug> values = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                values.addAll(character.getCharacterDrugs());
            }
        }
        return values;
    }

    @Override
    public CharacterDrug findById(Long id) {
        for (CharacterDrug value : findAll()) {
            if (id.equals(value.getId())) {
                return value;
            }
        }
        return null;
    }

    @Override
    public CharacterDrug save(CharacterDrug entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("character_drug"));
        }
        Long characterId = entity.getCharacter() == null ? null : entity.getCharacter().getId();

        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (characterId != null && characterId.equals(character.getId())) {
                    character.getCharacterDrugs().removeIf(value -> entity.getId().equals(value.getId()));
                    character.getCharacterDrugs().add(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("CharacterDrug must reference an existing character");
    }

    @Override
    public CharacterDrug update(CharacterDrug entity) {
        if (entity.getCharacter() == null || entity.getCharacter().getId() == null) {
            CharacterDrug existing = findById(entity.getId());
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
                if (character.getCharacterDrugs().removeIf(value -> id.equals(value.getId()))) {
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
