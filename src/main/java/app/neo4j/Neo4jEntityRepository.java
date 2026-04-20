package app.neo4j;

import app.dao.EntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;

public class Neo4jEntityRepository<T> implements EntityRepository<T> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;
    private final Class<T> entityClass;
    private final ObjectMapper objectMapper;
    private final String label;

    public Neo4jEntityRepository(Driver driver, Neo4jSequenceRepository sequenceRepository, Class<T> entityClass, ObjectMapper objectMapper) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
        this.entityClass = entityClass;
        this.objectMapper = objectMapper;
        this.label = Neo4jSupport.collectionLabel(entityClass);
    }

    @Override
    public List<T> findAll() {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run(
                    "MATCH (n:" + label + ") RETURN n ORDER BY n.id"
            ).list());
            return records.stream()
                    .map(record -> Neo4jSupport.toEntity(record.get("n").asNode(), entityClass, objectMapper))
                    .toList();
        }
    }

    @Override
    public T findById(Long id) {
        try (Session session = driver.session()) {
            java.util.List<Record> records = session.executeRead(tx -> tx.run(
                    "MATCH (n:" + label + " {id: $id}) RETURN n",
                    Map.of("id", id)
            ).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return record == null ? null : Neo4jSupport.toEntity(record.get("n").asNode(), entityClass, objectMapper);
        }
    }

    @Override
    public T save(T entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, label, entity);
        writeNode(id, entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, label, entity);
        writeNode(id, entity);
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        T entity = findById(id);
        if (entity == null) {
            return;
        }

        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n:" + label + " {id: $id}) DETACH DELETE n", Map.of("id", id));
                return null;
            });
        }
    }

    private void writeNode(Long id, T entity) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(
                        "MERGE (n:" + label + " {id: $id}) SET n = $props",
                        Map.of("id", id, "props", Neo4jNodeProperties.generic(id, entity))
                );
                return null;
            });
        }
    }
}
