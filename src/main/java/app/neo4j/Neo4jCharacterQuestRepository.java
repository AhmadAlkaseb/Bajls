package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import persistence.entity.CharacterQuest;
import persistence.entity.GameCharacter;
import persistence.entity.Quest;
import persistence.enums.QuestStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jCharacterQuestRepository implements EntityRepository<CharacterQuest> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jCharacterQuestRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<CharacterQuest> findAll() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:HAS_QUEST]->(q:Quest)
                    RETURN c, r, q
                    ORDER BY r.id
                    """).list(this::toEntity));
        }
    }

    @Override
    public CharacterQuest findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter)-[r:HAS_QUEST {id: $id}]->(q:Quest)
                    RETURN c, r, q
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return record == null ? null : toEntity(record);
        }
    }

    @Override
    public CharacterQuest save(CharacterQuest entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "CharacterQuest", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (c:GameCharacter {id: $characterId})
                        MERGE (q:Quest {id: $questId})
                        MERGE (c)-[r:HAS_QUEST {id: $id}]->(q)
                        SET r.status = $status,
                            r.acceptedAt = $acceptedAt
                        """, Neo4jSupport.props(
                        "characterId", entity.getCharacter().getId(),
                        "questId", entity.getQuest().getId(),
                        "id", id,
                        "status", entity.getStatus() == null ? null : entity.getStatus().name(),
                        "acceptedAt", entity.getAcceptedAt() == null ? null : entity.getAcceptedAt().toString()
                ));
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public CharacterQuest update(CharacterQuest entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (:GameCharacter)-[r:HAS_QUEST {id: $id}]->(:Quest) DELETE r", Map.of("id", id));
                return null;
            });
        }
    }

    private CharacterQuest toEntity(Record record) {
        Node characterNode = record.get("c").asNode();
        Relationship relationship = record.get("r").asRelationship();
        Node questNode = record.get("q").asNode();
        return CharacterQuest.builder()
                .id(Neo4jSupport.longValue(relationship, "id"))
                .character(GameCharacter.builder()
                        .id(Neo4jSupport.longValue(characterNode, "id"))
                        .characterDrugs(new ArrayList<>())
                        .characterQuests(new ArrayList<>())
                        .gangAffiliations(new ArrayList<>())
                        .build())
                .quest(Quest.builder()
                        .id(Neo4jSupport.longValue(questNode, "id"))
                        .characterQuests(new ArrayList<>())
                        .build())
                .status(Neo4jSupport.enumValue(relationship, "status", QuestStatus.class))
                .acceptedAt(Neo4jSupport.localDateTimeValue(relationship, "acceptedAt"))
                .build();
    }
}
