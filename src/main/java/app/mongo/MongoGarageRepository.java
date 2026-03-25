package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;
import persistence.entity.Profile;
import persistence.entity.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class MongoGarageRepository implements EntityRepository<Garage> {
    private final MongoProfileCollectionSupport support;

    public MongoGarageRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<Garage> findAll() {
        List<Garage> garages = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (character.getGarage() != null) {
                    garages.add(character.getGarage());
                }
            }
        }
        return garages;
    }

    @Override
    public Garage findById(Long id) {
        for (Garage garage : findAll()) {
            if (id.equals(garage.getId())) {
                return garage;
            }
        }
        return null;
    }

    @Override
    public Garage save(Garage entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("garages"));
        }
        if (entity.getVehicles() == null) {
            entity.setVehicles(new ArrayList<>());
        }
        for (Vehicle vehicle : entity.getVehicles()) {
            if (vehicle.getId() == null) {
                vehicle.setId(support.nextId("vehicles"));
            }
        }

        Long characterId = entity.getCharacter() == null ? null : entity.getCharacter().getId();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (characterId != null && characterId.equals(character.getId())) {
                    character.setGarage(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("Garage must reference an existing character");
    }

    @Override
    public Garage update(Garage entity) {
        if (entity.getCharacter() == null || entity.getCharacter().getId() == null) {
            Garage existing = findById(entity.getId());
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
                if (character.getGarage() != null && id.equals(character.getGarage().getId())) {
                    character.setGarage(null);
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
