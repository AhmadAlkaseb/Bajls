package app.neo4j;

import app.audit.AuditAction;
import app.audit.AuditContext;
import app.audit.AuditSnapshotUtil;
import app.dao.EntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import persistence.entity.AuditLog;

import java.time.LocalDateTime;
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
                    "MATCH (n:" + label + ") RETURN n.payload AS payload ORDER BY n.id"
            ).list());
            return Neo4jSupport.toEntities(records, entityClass, objectMapper);
        }
    }

    @Override
    public T findById(Long id) {
        try (Session session = driver.session()) {
            java.util.List<Record> records = session.executeRead(tx -> tx.run(
                    "MATCH (n:" + label + " {id: $id}) RETURN n.payload AS payload",
                    Map.of("id", id)
            ).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return Neo4jSupport.toEntity(record, entityClass, objectMapper);
        }
    }

    @Override
    public T save(T entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, label, entity);
        writeNode(id, entity);
        recordAuditLog(AuditAction.CREATE, null, AuditSnapshotUtil.toJson(entity), entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, label, entity);
        T previousEntity = findById(id);
        writeNode(id, entity);
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
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n:" + label + " {id: $id}) DETACH DELETE n", Map.of("id", id));
                return null;
            });
        }
    }

    private void writeNode(Long id, T entity) {
        String payload = Neo4jSupport.toJson(entity, objectMapper);
        Map<String, Object> properties = toNeo4jProperties(entity, id, payload);

        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(
                        "MERGE (n:" + label + " {id: $id}) SET n += $properties",
                        Map.of("id", id, "properties", properties)
                );
                return null;
            });
        }
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
                .id(sequenceRepository.nextValue("AuditLog"))
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

        String payload = Neo4jSupport.toJson(auditLog, objectMapper);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(
                        "MERGE (n:AuditLog {id: $id}) SET n.payload = $payload",
                        Map.of("id", auditLog.getId(), "payload", payload)
                );
                return null;
            });
        }
    }

    private Map<String, Object> toNeo4jProperties(T entity, Long id, String payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> raw = objectMapper.convertValue(entity, Map.class);

        Map<String, Object> props = new java.util.HashMap<>();

        raw.forEach((key, value) -> {
            Object converted = toNeo4jValue(value);
            if (converted != null) {
                props.put(key, converted);
            }
        });

        props.put("id", id);
        //props.put("payload", payload);

        return props;
    }

    private Object toNeo4jValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.math.BigDecimal bd) {
            return bd.doubleValue();
            // alternativt: return bd.toPlainString();
        }

        if (value instanceof java.math.BigInteger bi) {
            return bi.longValue();
        }

        if (value instanceof java.time.LocalDateTime ldt) {
            return ldt.toString();
        }

        if (value instanceof java.time.LocalDate ld) {
            return ld.toString();
        }

        if (value instanceof java.time.LocalTime lt) {
            return lt.toString();
        }

        if (value instanceof Enum<?> e) {
            return e.name();
        }

        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return Neo4jSupport.toJson(value, objectMapper);
        }

        return value;
    }
}