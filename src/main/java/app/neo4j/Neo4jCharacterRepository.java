package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;
import persistence.entity.House;
import persistence.entity.Profile;
import persistence.enums.EyeColorType;
import persistence.enums.GenderType;
import persistence.enums.SkinColorType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jCharacterRepository implements EntityRepository<GameCharacter> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jCharacterRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<GameCharacter> findAll() {
        try (Session session = driver.session()) {
            List<Long> ids = session.executeRead(tx -> tx.run(
                    "MATCH (c:GameCharacter) RETURN c.id AS id ORDER BY c.id"
            ).list(row -> row.get("id").asLong()));
            List<GameCharacter> characters = new ArrayList<>();
            for (Long id : ids) {
                GameCharacter character = findById(id);
                if (character != null) {
                    characters.add(character);
                }
            }
            return characters;
        }
    }

    @Override
    public GameCharacter findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (c:GameCharacter {id: $id})
                    OPTIONAL MATCH (p:Profile)-[:OWNS]->(c)
                    OPTIONAL MATCH (c)-[:HAS_HOUSE]->(h:House)
                    OPTIONAL MATCH (c)-[:HAS_GARAGE]->(g:Garage)
                    RETURN c, p, h, g
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            if (record == null) {
                return null;
            }
            return toCharacter(record);
        }
    }

    @Override
    public GameCharacter save(GameCharacter entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "GameCharacter", entity);
        House house = entity.getHouse();
        if (house != null && house.getId() == null) {
            house.setId(sequenceRepository.nextValue("House"));
        }
        Garage garage = entity.getGarage();
        if (garage != null && garage.getId() == null) {
            garage.setId(sequenceRepository.nextValue("Garage"));
        }

        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (c:GameCharacter {id: $id})
                        SET c.name = $name,
                            c.displayName = $name,
                            c.balance = $balance,
                            c.gender = $gender,
                            c.skincolor = $skincolor,
                            c.eyecolor = $eyecolor,
                            c.height = $height,
                            c.weight = $weight
                        """, Neo4jSupport.props(
                        "id", id,
                        "name", entity.getName(),
                        "balance", entity.getBalance() == null ? BigDecimal.ZERO.doubleValue() : entity.getBalance().doubleValue(),
                        "gender", entity.getGender() == null ? null : entity.getGender().name(),
                        "skincolor", entity.getSkincolor() == null ? null : entity.getSkincolor().name(),
                        "eyecolor", entity.getEyecolor() == null ? null : entity.getEyecolor().name(),
                        "height", entity.getHeight(),
                        "weight", entity.getWeight()
                ));

                Long profileId = entity.getProfile() == null ? null : entity.getProfile().getId();
                tx.run("MATCH (:Profile)-[r:OWNS]->(c:GameCharacter {id: $id}) DELETE r", Map.of("id", id));
                if (profileId != null) {
                    tx.run("""
                            MERGE (p:Profile {id: $profileId})
                            MERGE (c:GameCharacter {id: $id})
                            MERGE (p)-[:OWNS]->(c)
                            """, Map.of("profileId", profileId, "id", id));
                }

                tx.run("MATCH (c:GameCharacter {id: $id})-[r:HAS_HOUSE]->(:House) DELETE r", Map.of("id", id));
                if (house != null) {
                    if (!isReferenceOnly(house)) {
                        tx.run("MERGE (h:House {id: $id}) SET h += $props", Map.of(
                                "id", house.getId(),
                                "props", Map.of("name", "House " + house.getId(), "displayName", "House " + house.getId(),
                                        "amountRooms", house.getAmountRooms(), "amountBathrooms", house.getAmountBathrooms())));
                    } else {
                        tx.run("MERGE (h:House {id: $id})", Map.of("id", house.getId()));
                    }
                    tx.run("""
                            MATCH (c:GameCharacter {id: $characterId})
                            MATCH (h:House {id: $houseId})
                            MERGE (c)-[:HAS_HOUSE]->(h)
                            """, Map.of("characterId", id, "houseId", house.getId()));
                }

                tx.run("MATCH (c:GameCharacter {id: $id})-[r:HAS_GARAGE]->(:Garage) DELETE r", Map.of("id", id));
                if (garage != null) {
                    if (!isReferenceOnly(garage)) {
                        tx.run("MERGE (g:Garage {id: $id}) SET g += $props", Map.of(
                                "id", garage.getId(),
                                "props", Map.of("name", "Garage " + garage.getId(),
                                        "displayName", "Garage " + garage.getId(), "capacity", garage.getCapacity())));
                    } else {
                        tx.run("MERGE (g:Garage {id: $id})", Map.of("id", garage.getId()));
                    }
                    tx.run("""
                            MATCH (c:GameCharacter {id: $characterId})
                            MATCH (g:Garage {id: $garageId})
                            MERGE (c)-[:HAS_GARAGE]->(g)
                            """, Map.of("characterId", id, "garageId", garage.getId()));
                }
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public GameCharacter update(GameCharacter entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (c:GameCharacter {id: $id}) DETACH DELETE c", Map.of("id", id));
                return null;
            });
        }
    }

    private GameCharacter toCharacter(Record record) {
        Node characterNode = record.get("c").asNode();
        Node profileNode = record.get("p").isNull() ? null : record.get("p").asNode();
        Node houseNode = record.get("h").isNull() ? null : record.get("h").asNode();
        Node garageNode = record.get("g").isNull() ? null : record.get("g").asNode();

        return GameCharacter.builder()
                .id(Neo4jSupport.longValue(characterNode, "id"))
                .name(Neo4jSupport.stringValue(characterNode, "name"))
                .balance(defaultBalance(Neo4jSupport.decimalValue(characterNode, "balance")))
                .profile(profileNode == null ? null : Profile.builder().id(Neo4jSupport.longValue(profileNode, "id")).characters(new ArrayList<>()).build())
                .gender(Neo4jSupport.enumValue(characterNode, "gender", GenderType.class))
                .skincolor(Neo4jSupport.enumValue(characterNode, "skincolor", SkinColorType.class))
                .eyecolor(Neo4jSupport.enumValue(characterNode, "eyecolor", EyeColorType.class))
                .height(Neo4jSupport.stringValue(characterNode, "height"))
                .weight(Neo4jSupport.stringValue(characterNode, "weight"))
                .house(houseNode == null ? null : House.builder()
                        .id(Neo4jSupport.longValue(houseNode, "id"))
                        .amountRooms(readInt(houseNode, "amountRooms"))
                        .amountBathrooms(readInt(houseNode, "amountBathrooms"))
                        .build())
                .garage(garageNode == null ? null : Garage.builder()
                        .id(Neo4jSupport.longValue(garageNode, "id"))
                        .capacity(readInt(garageNode, "capacity"))
                        .vehicles(new ArrayList<>())
                        .build())
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build();
    }

    private static boolean isReferenceOnly(House house) {
        return house.getId() != null && house.getAmountRooms() == 0 && house.getAmountBathrooms() == 0;
    }

    private static boolean isReferenceOnly(Garage garage) {
        return garage.getId() != null && garage.getCapacity() == 0;
    }

    private static int readInt(Node node, String key) {
        return node.get(key).isNull() ? 0 : node.get(key).asInt();
    }

    private static BigDecimal defaultBalance(BigDecimal balance) {
        return balance == null ? BigDecimal.ZERO : balance;
    }
}
