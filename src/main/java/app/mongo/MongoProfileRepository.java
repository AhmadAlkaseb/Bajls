package app.mongo;

import app.auth.PasswordSupport;
import app.dao.ProfileEntityRepository;
import app.dto.LoginResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import persistence.entity.Profile;

public class MongoProfileRepository extends MongoEntityRepository<Profile> implements ProfileEntityRepository {
    private final MongoProfileCollectionSupport support;

    public MongoProfileRepository(MongoDatabase database, ObjectMapper objectMapper) {
        super(database, MongoCollections.PROFILE, Profile.class, objectMapper);
        this.support = new MongoProfileCollectionSupport(database);
    }

    @Override
    public java.util.List<Profile> findAll() {
        return support.findAllProfiles();
    }

    @Override
    public Profile findById(Long id) {
        return support.findProfileById(id);
    }

    @Override
    public Profile save(Profile entity) {
        return support.saveProfile(entity);
    }

    @Override
    public Profile update(Profile entity) {
        return support.saveProfile(entity);
    }

    @Override
    public void deleteById(Long id) {
        support.deleteProfile(id);
    }

    @Override
    public LoginResponseDTO authenticate(String username, String password) {
        Profile profile = support.findProfileByUsername(username);
        if (profile == null || !PasswordSupport.matches(password, profile.getPassword())) {
            return null;
        }
        return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
    }
}
