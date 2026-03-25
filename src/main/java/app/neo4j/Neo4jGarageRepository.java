package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jGarageRepository implements EntityRepository<Garage> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jGarageRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<Garage> findAll() {
        try (Session session = driver.session()) {
            List<Long> ids = session.executeRead(tx -> tx.run(
                    "MATCH (g:Garage) RETURN g.id AS id ORDER BY g.id"
            ).list(row -> row.get("id").asLong()));
            List<Garage> garages = new ArrayList<>();
            for (Long id : ids) {
                Garage garage = findById(id);
                if (garage != null) {
                    garages.add(garage);
                }
            }
            return garages;
        }
    }

    @Override
    public Garage findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (g:Garage {id: $id})
                    OPTIONAL MATCH (c:GameCharacter)-[:HAS_GARAGE]->(g)
                    RETURN g, c
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            if (record == null) {
                return null;
            }
            Node garageNode = record.get("g").asNode();
            Node characterNode = record.get("c").isNull() ? null : record.get("c").asNode();
            return Garage.builder()
                    .id(Neo4jSupport.longValue(garageNode, "id"))
                    .capacity(garageNode.get("capacity").isNull() ? 0 : garageNode.get("capacity").asInt())
                    .character(characterNode == null ? null : GameCharacter.builder()
                            .id(Neo4jSupport.longValue(characterNode, "id"))
                            .characterDrugs(new ArrayList<>())
                            .characterQuests(new ArrayList<>())
                            .gangAffiliations(new ArrayList<>())
                            .build())
                    .vehicles(new ArrayList<>())
                    .build();
        }
    }

    @Override
    public Garage save(Garage entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "Garage", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (g:Garage {id: $id})
                        SET g.capacity = $capacity
                        """, Neo4jSupport.props(
                        "id", id,
                        "capacity", entity.getCapacity()
                ));
                if (entity.getCharacter() != null && entity.getCharacter().getId() != null) {
                    tx.run("MATCH (c:GameCharacter {id: $id})-[r:HAS_GARAGE]->(:Garage) DELETE r", Map.of("id", entity.getCharacter().getId()));
                    tx.run("""
                            MATCH (g:Garage {id: $garageId})
                            MATCH (c:GameCharacter {id: $characterId})
                            MERGE (c)-[:HAS_GARAGE]->(g)
                            """, Map.of("characterId", entity.getCharacter().getId(), "garageId", id));
                }
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public Garage update(Garage entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (g:Garage {id: $id}) DETACH DELETE g", Map.of("id", id));
                return null;
            });
        }
    }
}
