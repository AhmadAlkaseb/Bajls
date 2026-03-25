package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jGangAffiliationRepository implements EntityRepository<GangAffiliation> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jGangAffiliationRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<GangAffiliation> findAll() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:MEMBER_OF]->(g:Gang)
                    RETURN c, r, g
                    ORDER BY r.id
                    """).list(this::toEntity));
        }
    }

    @Override
    public GangAffiliation findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:MEMBER_OF {id: $id}]->(g:Gang)
                    RETURN c, r, g
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return record == null ? null : toEntity(record);
        }
    }

    @Override
    public GangAffiliation save(GangAffiliation entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "GangAffiliation", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (c:GameCharacter {id: $characterId})
                        MERGE (g:Gang {id: $gangId})
                        MERGE (c)-[r:MEMBER_OF {id: $id}]->(g)
                        SET r.joinDate = $joinDate
                        """, Neo4jSupport.props(
                        "characterId", entity.getCharacter().getId(),
                        "gangId", entity.getGang().getId(),
                        "id", id,
                        "joinDate", entity.getJoinDate() == null ? null : entity.getJoinDate().toString()
                ));
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public GangAffiliation update(GangAffiliation entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (:GameCharacter)-[r:MEMBER_OF {id: $id}]->(:Gang) DELETE r", Map.of("id", id));
                return null;
            });
        }
    }

    private GangAffiliation toEntity(Record record) {
        Node characterNode = record.get("c").asNode();
        Relationship relationship = record.get("r").asRelationship();
        Node gangNode = record.get("g").asNode();
        return GangAffiliation.builder()
                .id(Neo4jSupport.longValue(relationship, "id"))
                .character(GameCharacter.builder()
                        .id(Neo4jSupport.longValue(characterNode, "id"))
                        .characterDrugs(new ArrayList<>())
                        .characterQuests(new ArrayList<>())
                        .gangAffiliations(new ArrayList<>())
                        .build())
                .gang(Gang.builder()
                        .id(Neo4jSupport.longValue(gangNode, "id"))
                        .affiliations(new ArrayList<>())
                        .build())
                .joinDate(Neo4jSupport.localDateValue(relationship, "joinDate"))
                .build();
    }
}
