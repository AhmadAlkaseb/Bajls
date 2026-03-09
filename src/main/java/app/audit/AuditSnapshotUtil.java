package app.audit;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class AuditSnapshotUtil {

    private AuditSnapshotUtil() {
    }

    public static String toJson(Object entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        Class<?> type = entity.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || values.containsKey(field.getName())) {
                    continue;
                }
                Object value = extractValue(entity, field);
                if (value != null || shouldIncludeNull(field)) {
                    values.put(field.getName(), value);
                }
            }
            type = type.getSuperclass();
        }

        StringJoiner json = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            json.add(quote(entry.getKey()) + ": " + toJsonValue(entry.getValue()));
        }
        return json.toString();
    }

    public static Long getEntityId(Object entity) {
        if (entity == null) {
            return null;
        }

        Class<?> type = entity.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                    Object value = extractRawValue(entity, field);
                    if (value instanceof Number number) {
                        return number.longValue();
                    }
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static Object extractValue(Object entity, Field field) {
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            Object association = extractRawValue(entity, field);
            return getEntityId(association);
        }

        Object value = extractRawValue(entity, field);
        if (value == null) {
            return null;
        }

        if (isSimpleValue(value)) {
            return value;
        }

        if (field.getType().isAnnotationPresent(Entity.class)) {
            return getEntityId(value);
        }

        return String.valueOf(value);
    }

    private static boolean shouldIncludeNull(Field field) {
        return field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(JoinColumn.class);
    }

    private static Object extractRawValue(Object entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to read field " + field.getName(), e);
        }
    }

    private static boolean isSimpleValue(Object value) {
        return value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof Temporal
                || value instanceof BigDecimal
                || value instanceof java.util.Date;
    }

    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 32) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
