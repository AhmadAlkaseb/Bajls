package app.neo4j;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import java.lang.reflect.Field;
import java.util.List;

public final class Neo4jSupport {

    private Neo4jSupport() {
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(MapperFeature.USE_ANNOTATIONS, false);
    }

    public static <T> T toEntity(Value jsonValue, Class<T> type, ObjectMapper objectMapper) {
        if (jsonValue == null || jsonValue.isNull()) {
            return null;
        }
        return objectMapper.convertValue(objectMapper.convertValue(parseJson(jsonValue.asString(), objectMapper), Object.class), type);
    }

    public static <T> T toEntity(Record record, Class<T> type, ObjectMapper objectMapper) {
        if (record == null) {
            return null;
        }
        return toEntity(record.get("payload"), type, objectMapper);
    }

    public static String toJson(Object entity, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize entity", e);
        }
    }

    public static Long ensureEntityId(Neo4jSequenceRepository sequenceRepository, String sequenceName, Object entity) {
        Long id = readEntityId(entity);
        if (id != null) {
            return id;
        }

        Long nextId = sequenceRepository.nextValue(sequenceName);
        writeEntityId(entity, nextId);
        return nextId;
    }

    public static String collectionLabel(Class<?> entityClass) {
        return entityClass.getSimpleName();
    }

    public static <T> List<T> toEntities(List<Record> records, Class<T> type, ObjectMapper objectMapper) {
        return records.stream()
                .map(record -> toEntity(record, type, objectMapper))
                .toList();
    }

    private static Object parseJson(String json, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize entity", e);
        }
    }

    private static Long readEntityId(Object entity) {
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
}
