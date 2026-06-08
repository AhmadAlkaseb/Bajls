package app.neo4j;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

public class Neo4jSequenceRepository {
    private final Driver driver;

    public Neo4jSequenceRepository(Driver driver) {
        this.driver = driver;
    }

    public Long nextValue(String sequenceName) {
        try (Session session = driver.session()) {
            Record record = session.executeRead(tx -> tx.run(nextIdQuery(sequenceName)).single());
            return record.get("value").asLong();
        }
    }

    private String nextIdQuery(String sequenceName) {
        return switch (sequenceName) {
            case "CharacterDrug" -> "MATCH ()-[r:HAS_DRUG]->() RETURN coalesce(max(r.id), 0) + 1 AS value";
            case "CharacterQuest" -> "MATCH ()-[r:HAS_QUEST]->() RETURN coalesce(max(r.id), 0) + 1 AS value";
            case "GangAffiliation" -> "MATCH ()-[r:MEMBER_OF]->() RETURN coalesce(max(r.id), 0) + 1 AS value";
            default -> "MATCH (n:" + sequenceName + ") RETURN coalesce(max(n.id), 0) + 1 AS value";
        };
    }
}
