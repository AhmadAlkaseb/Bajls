package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import persistence.entity.CharacterDrug;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jCharacterDrugRepository implements EntityRepository<CharacterDrug> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jCharacterDrugRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<CharacterDrug> findAll() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:HAS_DRUG]->(d:Drug)
                    RETURN c, r, d
                    ORDER BY r.id
                    """).list(this::toEntity));
        }
    }

    @Override
    public CharacterDrug findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:HAS_DRUG {id: $id}]->(d:Drug)
                    RETURN c, r, d
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return record == null ? null : toEntity(record);
        }
    }

    @Override
    public CharacterDrug save(CharacterDrug entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "CharacterDrug", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (c:GameCharacter {id: $characterId})
                        MERGE (d:Drug {id: $drugId})
                        MERGE (c)-[r:HAS_DRUG {id: $id}]->(d)
                        SET r.quantity = $quantity
                        """, Neo4jSupport.props(
                        "characterId", entity.getCharacter().getId(),
                        "drugId", entity.getDrug().getId(),
                        "id", id,
                        "quantity", entity.getQuantity()
                ));
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public CharacterDrug update(CharacterDrug entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (:GameCharacter)-[r:HAS_DRUG {id: $id}]->(:Drug) DELETE r", Map.of("id", id));
                return null;
            });
        }
    }

    private CharacterDrug toEntity(Record record) {
        Node characterNode = record.get("c").asNode();
        Relationship relationship = record.get("r").asRelationship();
        Node drugNode = record.get("d").asNode();
        return CharacterDrug.builder()
                .id(Neo4jSupport.longValue(relationship, "id"))
                .character(GameCharacter.builder()
                        .id(Neo4jSupport.longValue(characterNode, "id"))
                        .characterDrugs(new ArrayList<>())
                        .characterQuests(new ArrayList<>())
                        .gangAffiliations(new ArrayList<>())
                        .build())
                .drug(Drug.builder()
                        .id(Neo4jSupport.longValue(drugNode, "id"))
                        .characterDrugs(new ArrayList<>())
                        .build())
                .quantity(Neo4jSupport.intValue(relationship, "quantity"))
                .build();
    }
}
