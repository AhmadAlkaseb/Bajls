package app.neo4j;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.Map;

public final class Neo4jSupport {

    private Neo4jSupport() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.USE_ANNOTATIONS)
                .build();
    }

    public static <T> T toEntity(Node node, Class<T> type, ObjectMapper objectMapper) {
        return objectMapper.convertValue(node.asMap(), type);
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

    public static Long longValue(Node node, String key) {
        return nullableLong(node.get(key));
    }

    public static Long longValue(Relationship relationship, String key) {
        return nullableLong(relationship.get(key));
    }

    public static String stringValue(Node node, String key) {
        return nullableString(node.get(key));
    }

    public static String stringValue(Relationship relationship, String key) {
        return nullableString(relationship.get(key));
    }

    public static Integer intValue(Relationship relationship, String key) {
        Value value = relationship.get(key);
        return value == null || value.isNull() ? null : value.asInt();
    }

    public static BigDecimal decimalValue(Node node, String key) {
        return nullableBigDecimal(node.get(key));
    }

    public static <E extends Enum<E>> E enumValue(Node node, String key, Class<E> type) {
        return enumValue(node.get(key), type);
    }

    public static <E extends Enum<E>> E enumValue(Relationship relationship, String key, Class<E> type) {
        return enumValue(relationship.get(key), type);
    }

    public static <E extends Enum<E>> E enumValue(Value value, Class<E> type) {
        String raw = nullableString(value);
        return raw == null ? null : Enum.valueOf(type, raw);
    }

    public static java.time.LocalDateTime localDateTimeValue(Relationship relationship, String key) {
        String raw = stringValue(relationship, key);
        return raw == null ? null : java.time.LocalDateTime.parse(raw);
    }

    public static java.time.LocalDate localDateValue(Relationship relationship, String key) {
        String raw = stringValue(relationship, key);
        return raw == null ? null : java.time.LocalDate.parse(raw);
    }

    public static Map<String, Object> props(Object... values) {
        return Map.ofEntries(buildEntries(values));
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<String, Object>[] buildEntries(Object... values) {
        Map.Entry<String, Object>[] entries = new Map.Entry[values.length / 2];
        for (int i = 0; i < values.length; i += 2) {
            entries[i / 2] = Map.entry((String) values[i], values[i + 1]);
        }
        return entries;
    }

    private static Long nullableLong(Value value) {
        return value == null || value.isNull() ? null : value.asLong();
    }

    private static String nullableString(Value value) {
        return value == null || value.isNull() ? null : value.asString();
    }

    private static BigDecimal nullableBigDecimal(Value value) {
        if (value == null || value.isNull()) {
            return null;
        }
        Object raw = value.asObject();
        if (raw instanceof BigDecimal decimal) {
            return decimal;
        }
        if (raw instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(raw));
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
