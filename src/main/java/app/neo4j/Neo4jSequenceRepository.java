package app.neo4j;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.Map;

public class Neo4jSequenceRepository {
    private final Driver driver;

    public Neo4jSequenceRepository(Driver driver) {
        this.driver = driver;
    }

    public Long nextValue(String sequenceName) {
        try (Session session = driver.session()) {
            Record record = session.executeWrite(tx -> tx.run(
                    """
                    MERGE (s:_Sequence {name: $name})
                    ON CREATE SET s.value = 1
                    ON MATCH SET s.value = s.value + 1
                    RETURN s.value AS value
                    """,
                    Map.of("name", sequenceName)
            ).single());
            return record.get("value").asLong();
        }
    }
}
