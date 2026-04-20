package app.mongo;

import org.bson.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class MongoProfileValues {
    private MongoProfileValues() {
    }

    static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    static List<Document> documents(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Document> documents = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Document document) {
                documents.add(document);
            }
        }
        return documents;
    }

    static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    static Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    static BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(Objects.toString(value));
    }

    static LocalDateTime localDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    static LocalDate localDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
