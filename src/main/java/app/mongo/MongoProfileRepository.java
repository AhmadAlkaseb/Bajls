package app.mongo;

import app.dao.ProfileEntityRepository;
import app.dto.LoginResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import persistence.entity.Profile;

public class MongoProfileRepository extends MongoEntityRepository<Profile> implements ProfileEntityRepository {
    private final com.mongodb.client.MongoCollection<Document> collection;
    private final ObjectMapper objectMapper;

    public MongoProfileRepository(MongoDatabase database, ObjectMapper objectMapper) {
        super(database, "profiles", Profile.class, objectMapper);
        this.collection = database.getCollection("profiles");
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginResponseDTO authenticate(String username, String password) {
        Document document = collection.find(
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("password", password)
                )
        ).first();

        Profile profile = MongoSupport.toEntity(document, Profile.class, objectMapper);
        if (profile == null) {
            return null;
        }

        return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
    }
}
