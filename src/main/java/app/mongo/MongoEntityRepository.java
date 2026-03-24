package app.mongo;

import app.audit.AuditAction;
import app.audit.AuditContext;
import app.audit.AuditSnapshotUtil;
import app.dao.EntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import persistence.entity.AuditLog;

import java.time.LocalDateTime;
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
        collection.insertOne(MongoSupport.toDocument(entity, objectMapper));
        recordAuditLog(AuditAction.CREATE, null, AuditSnapshotUtil.toJson(entity), entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        Long id = MongoSupport.ensureEntityId(database, collection.getNamespace().getCollectionName(), entity);
        T previousEntity = findById(id);
        collection.replaceOne(
                Filters.eq("id", id),
                MongoSupport.toDocument(entity, objectMapper),
                new ReplaceOptions().upsert(true)
        );
        recordAuditLog(AuditAction.UPDATE, AuditSnapshotUtil.toJson(previousEntity), AuditSnapshotUtil.toJson(entity), entity);
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        T entity = findById(id);
        if (entity == null) {
            return;
        }

        recordAuditLog(AuditAction.DELETE, AuditSnapshotUtil.toJson(entity), null, entity);
        collection.deleteOne(Filters.eq("id", id));
    }

    private void recordAuditLog(AuditAction action, String oldValues, String newValues, T targetEntity) {
        if (AuditLog.class.equals(entityClass)) {
            return;
        }

        AuditContext.AuditMetadata metadata = AuditContext.getCurrent();
        if (metadata == null) {
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .id(MongoSupport.ensureEntityId(database, "audit_log", new AuditLog()))
                .actorProfileId(metadata.getActorProfileId())
                .actorUsername(metadata.getActorUsername())
                .actorRole(metadata.getActorRole())
                .action(action.name())
                .entityName(entityClass.getSimpleName())
                .entityId(AuditSnapshotUtil.getEntityId(targetEntity))
                .requestMethod(metadata.getRequestMethod())
                .requestPath(metadata.getRequestPath())
                .oldValues(oldValues)
                .newValues(newValues)
                .changedAt(LocalDateTime.now())
                .build();

        database.getCollection("audit_log").insertOne(MongoSupport.toDocument(auditLog, objectMapper));
    }
}
