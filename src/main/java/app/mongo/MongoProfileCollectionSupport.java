package app.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import persistence.entity.Profile;

import java.util.ArrayList;
import java.util.List;

public final class MongoProfileCollectionSupport {
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoProfileCollectionSupport(MongoDatabase database) {
        this.database = database;
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

    public Profile findProfileByCredentials(String username, String password) {
        return MongoProfileEntityMapper.toProfile(collection.find(Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password)
        )).first());
    }

    public Profile saveProfile(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(MongoSupport.nextSequenceValue(database, MongoCollections.PROFILE));
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
        return MongoSupport.nextSequenceValue(database, sequenceName);
    }
}
