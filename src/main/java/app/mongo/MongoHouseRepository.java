package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.GameCharacter;
import persistence.entity.House;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public class MongoHouseRepository implements EntityRepository<House> {
    private final MongoProfileCollectionSupport support;

    public MongoHouseRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<House> findAll() {
        List<House> houses = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (character.getHouse() != null) {
                    houses.add(character.getHouse());
                }
            }
        }
        return houses;
    }

    @Override
    public House findById(Long id) {
        for (House house : findAll()) {
            if (id.equals(house.getId())) {
                return house;
            }
        }
        return null;
    }

    @Override
    public House save(House entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("houses"));
        }
        Long characterId = entity.getCharacter() == null ? null : entity.getCharacter().getId();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (characterId != null && characterId.equals(character.getId())) {
                    character.setHouse(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("House must reference an existing character");
    }

    @Override
    public House update(House entity) {
        if (entity.getCharacter() == null || entity.getCharacter().getId() == null) {
            House existing = findById(entity.getId());
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
                if (character.getHouse() != null && id.equals(character.getHouse().getId())) {
                    character.setHouse(null);
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
