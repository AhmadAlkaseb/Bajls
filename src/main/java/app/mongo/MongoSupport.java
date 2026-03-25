package app.mongo;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MongoSupport {
    private static final String COUNTERS_COLLECTION = "_counters";

    private MongoSupport() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.USE_ANNOTATIONS)
                .build();
    }

    static <T> List<T> findAll(MongoCollection<Document> collection, Class<T> type, ObjectMapper objectMapper) {
        List<T> results = new ArrayList<>();
        for (Document document : collection.find()) {
            results.add(toEntity(document, type, objectMapper));
        }
        return results;
    }

    static <T> T findById(MongoCollection<Document> collection, Long id, Class<T> type, ObjectMapper objectMapper) {
        Document document = collection.find(Filters.eq("id", id)).first();
        return toEntity(document, type, objectMapper);
    }

    static <T> Document toDocument(T entity, ObjectMapper objectMapper) {
        @SuppressWarnings("unchecked")
        Map<String, Object> values = objectMapper.convertValue(entity, Map.class);
        return new Document(values);
    }

    static <T> T toEntity(Document document, Class<T> type, ObjectMapper objectMapper) {
        if (document == null) {
            return null;
        }
        return objectMapper.convertValue(document, type);
    }

    static Long ensureEntityId(MongoDatabase database, String collectionName, Object entity) {
        Long id = readEntityId(entity);
        if (id != null) {
            return id;
        }

        Long nextId = nextSequenceValue(database, collectionName);
        writeEntityId(entity, nextId);
        return nextId;
    }

    static Long readEntityId(Object entity) {
        try {
            Field field = findIdField(entity.getClass());
            field.setAccessible(true);
            Object value = field.get(entity);
            return value instanceof Number number ? number.longValue() : null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to read entity id", e);
        }
    }

    private static void writeEntityId(Object entity, Long id) {
        try {
            Field field = findIdField(entity.getClass());
            field.setAccessible(true);
            field.set(entity, id);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to write entity id", e);
        }
    }

    private static Field findIdField(Class<?> type) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField("id");
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("Entity class does not define an id field: " + type.getName());
    }

    static Long nextSequenceValue(MongoDatabase database, String collectionName) {
        MongoCollection<Document> counters = database.getCollection(COUNTERS_COLLECTION);
        Document counter = counters.findOneAndUpdate(
                Filters.eq("_id", collectionName),
                new Document("$inc", new Document("seq", 1L)),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );
        return counter.getLong("seq");
    }
}
