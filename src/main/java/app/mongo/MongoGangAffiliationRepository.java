package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.GameCharacter;
import persistence.entity.GangAffiliation;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public class MongoGangAffiliationRepository implements EntityRepository<GangAffiliation> {
    private final MongoProfileCollectionSupport support;

    public MongoGangAffiliationRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<GangAffiliation> findAll() {
        List<GangAffiliation> values = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                values.addAll(character.getGangAffiliations());
            }
        }
        return values;
    }

    @Override
    public GangAffiliation findById(Long id) {
        for (GangAffiliation value : findAll()) {
            if (id.equals(value.getId())) {
                return value;
            }
        }
        return null;
    }

    @Override
    public GangAffiliation save(GangAffiliation entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("gang_affiliations"));
        }
        Long characterId = entity.getCharacter() == null ? null : entity.getCharacter().getId();

        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (characterId != null && characterId.equals(character.getId())) {
                    character.getGangAffiliations().removeIf(value -> entity.getId().equals(value.getId()));
                    character.getGangAffiliations().add(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("GangAffiliation must reference an existing character");
    }

    @Override
    public GangAffiliation update(GangAffiliation entity) {
        if (entity.getCharacter() == null || entity.getCharacter().getId() == null) {
            GangAffiliation existing = findById(entity.getId());
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
                if (character.getGangAffiliations().removeIf(value -> id.equals(value.getId()))) {
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
