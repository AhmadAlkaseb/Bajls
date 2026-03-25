package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import persistence.entity.GameCharacter;
import persistence.entity.House;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jHouseRepository implements EntityRepository<House> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jHouseRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<House> findAll() {
        try (Session session = driver.session()) {
            List<Long> ids = session.executeRead(tx -> tx.run(
                    "MATCH (h:House) RETURN h.id AS id ORDER BY h.id"
            ).list(row -> row.get("id").asLong()));
            List<House> houses = new ArrayList<>();
            for (Long id : ids) {
                House house = findById(id);
                if (house != null) {
                    houses.add(house);
                }
            }
            return houses;
        }
    }

    @Override
    public House findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (h:House {id: $id})
                    OPTIONAL MATCH (c:GameCharacter)-[:HAS_HOUSE]->(h)
                    RETURN h, c
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            if (record == null) {
                return null;
            }
            Node houseNode = record.get("h").asNode();
            Node characterNode = record.get("c").isNull() ? null : record.get("c").asNode();
            return House.builder()
                    .id(Neo4jSupport.longValue(houseNode, "id"))
                    .amountRooms(houseNode.get("amountRooms").isNull() ? 0 : houseNode.get("amountRooms").asInt())
                    .amountBathrooms(houseNode.get("amountBathrooms").isNull() ? 0 : houseNode.get("amountBathrooms").asInt())
                    .character(characterNode == null ? null : GameCharacter.builder()
                            .id(Neo4jSupport.longValue(characterNode, "id"))
                            .characterDrugs(new ArrayList<>())
                            .characterQuests(new ArrayList<>())
                            .gangAffiliations(new ArrayList<>())
                            .build())
                    .build();
        }
    }

    @Override
    public House save(House entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "House", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (h:House {id: $id})
                        SET h.amountRooms = $amountRooms,
                            h.amountBathrooms = $amountBathrooms
                        """, Neo4jSupport.props(
                        "id", id,
                        "amountRooms", entity.getAmountRooms(),
                        "amountBathrooms", entity.getAmountBathrooms()
                ));
                if (entity.getCharacter() != null && entity.getCharacter().getId() != null) {
                    tx.run("MATCH (c:GameCharacter {id: $id})-[r:HAS_HOUSE]->(:House) DELETE r", Map.of("id", entity.getCharacter().getId()));
                    tx.run("""
                            MATCH (h:House {id: $houseId})
                            MATCH (c:GameCharacter {id: $characterId})
                            MERGE (c)-[:HAS_HOUSE]->(h)
                            """, Map.of("characterId", entity.getCharacter().getId(), "houseId", id));
                }
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public House update(House entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (h:House {id: $id}) DETACH DELETE h", Map.of("id", id));
                return null;
            });
        }
    }
}
