package app.neo4j;

import persistence.entity.Drug;
import persistence.entity.Gang;
import persistence.entity.Quest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Neo4jNodeProperties {
    private Neo4jNodeProperties() {
    }

    public static Map<String, Object> generic(Long id, Object entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", id);
        addEntityValues(values, entity);
        return values;
    }

    private static void addEntityValues(Map<String, Object> values, Object entity) {
        if (entity instanceof Drug drug) {
            values.put("name", drug.getName());
            values.put("displayName", drug.getName());
            values.put("type", drug.getType() == null ? null : drug.getType().name());
        }
        if (entity instanceof Gang gang) {
            values.put("name", gang.getName());
            values.put("displayName", gang.getName());
            values.put("type", gang.getType() == null ? null : gang.getType().name());
        }
        if (entity instanceof Quest quest) {
            values.put("name", quest.getTitle());
            values.put("displayName", quest.getTitle());
            values.put("title", quest.getTitle());
            values.put("description", quest.getDescription());
            values.put("reward", quest.getReward() == null ? 0.0 : quest.getReward().doubleValue());
        }
    }
}
