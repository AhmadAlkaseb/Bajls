package app.mongo;

import app.dao.EntityRepository;
import com.mongodb.client.MongoDatabase;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;
import persistence.entity.Profile;
import persistence.entity.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class MongoVehicleRepository implements EntityRepository<Vehicle> {
    private final MongoProfileCollectionSupport support;

    public MongoVehicleRepository(MongoDatabase database) {
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                if (character.getGarage() != null) {
                    vehicles.addAll(character.getGarage().getVehicles());
                }
            }
        }
        return vehicles;
    }

    @Override
    public Vehicle findById(Long id) {
        for (Vehicle vehicle : findAll()) {
            if (id.equals(vehicle.getId())) {
                return vehicle;
            }
        }
        return null;
    }

    @Override
    public Vehicle save(Vehicle entity) {
        if (entity.getId() == null) {
            entity.setId(support.nextId("vehicles"));
        }
        Long garageId = entity.getGarage() == null ? null : entity.getGarage().getId();

        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                Garage garage = character.getGarage();
                if (garage != null && garageId != null && garageId.equals(garage.getId())) {
                    garage.getVehicles().removeIf(vehicle -> entity.getId().equals(vehicle.getId()));
                    garage.getVehicles().add(entity);
                    support.saveProfile(profile);
                    return findById(entity.getId());
                }
            }
        }
        throw new IllegalArgumentException("Vehicle must reference an existing garage");
    }

    @Override
    public Vehicle update(Vehicle entity) {
        if (entity.getGarage() == null || entity.getGarage().getId() == null) {
            Vehicle existing = findById(entity.getId());
            if (existing != null) {
                entity.setGarage(existing.getGarage());
            }
        }
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        for (Profile profile : support.findAllProfiles()) {
            for (GameCharacter character : profile.getCharacters()) {
                Garage garage = character.getGarage();
                if (garage != null && garage.getVehicles().removeIf(vehicle -> id.equals(vehicle.getId()))) {
                    support.saveProfile(profile);
                    return;
                }
            }
        }
    }
}
