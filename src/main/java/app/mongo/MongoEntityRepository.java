package app.mongo;

import app.dao.EntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import persistence.entity.Drug;
import persistence.entity.Gang;
import persistence.entity.Quest;

import java.util.List;

public class MongoEntityRepository<T> implements EntityRepository<T> {
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final Class<T> entityClass;
    private final ObjectMapper objectMapper;

    public MongoEntityRepository(MongoDatabase database, String collectionName, Class<T> entityClass, ObjectMapper objectMapper) {
        this.database = database;
        this.collection = database.getCollection(collectionName);
        this.entityClass = entityClass;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<T> findAll() {
        return MongoSupport.findAll(collection, entityClass, objectMapper);
    }

    @Override
    public T findById(Long id) {
        return MongoSupport.findById(collection, id, entityClass, objectMapper);
    }

    @Override
    public T save(T entity) {
        MongoSupport.ensureEntityId(database, collection.getNamespace().getCollectionName(), entity);
        collection.insertOne(toDocument(entity));
        return entity;
    }

    @Override
    public T update(T entity) {
        Long id = MongoSupport.ensureEntityId(database, collection.getNamespace().getCollectionName(), entity);
        collection.replaceOne(
                Filters.eq("id", id),
                toDocument(entity),
                new ReplaceOptions().upsert(true)
        );
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        T entity = findById(id);
        if (entity == null) {
            return;
        }

        collection.deleteOne(Filters.eq("id", id));
    }

    private Document toDocument(T entity) {
        Document document = MongoSupport.toDocument(entity, objectMapper);
        if (Drug.class.equals(entityClass)) {
            document.remove("characterDrugs");
        }
        if (Quest.class.equals(entityClass)) {
            document.remove("characterQuests");
        }
        if (Gang.class.equals(entityClass)) {
            document.remove("affiliations");
        }
        return document;
    }
}
