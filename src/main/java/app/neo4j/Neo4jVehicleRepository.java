package app.neo4j;

import app.dao.EntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import persistence.entity.Garage;
import persistence.entity.Vehicle;
import persistence.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jVehicleRepository implements EntityRepository<Vehicle> {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jVehicleRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<Vehicle> findAll() {
        try (Session session = driver.session()) {
            List<Long> ids = session.executeRead(tx -> tx.run(
                    "MATCH (v:Vehicle) RETURN v.id AS id ORDER BY v.id"
            ).list(row -> row.get("id").asLong()));
            List<Vehicle> vehicles = new ArrayList<>();
            for (Long id : ids) {
                Vehicle vehicle = findById(id);
                if (vehicle != null) {
                    vehicles.add(vehicle);
                }
            }
            return vehicles;
        }
    }

    @Override
    public Vehicle findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (v:Vehicle {id: $id})
                    OPTIONAL MATCH (g:Garage)-[:STORES]->(v)
                    RETURN v, g
                    """, Map.of("id", id)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            if (record == null) {
                return null;
            }
            Node vehicleNode = record.get("v").asNode();
            Node garageNode = record.get("g").isNull() ? null : record.get("g").asNode();
            return Vehicle.builder()
                    .id(Neo4jSupport.longValue(vehicleNode, "id"))
                    .garage(garageNode == null ? null : Garage.builder()
                            .id(Neo4jSupport.longValue(garageNode, "id"))
                            .vehicles(new ArrayList<>())
                            .build())
                    .model(Neo4jSupport.stringValue(vehicleNode, "model"))
                    .type(Neo4jSupport.enumValue(vehicleNode, "type", VehicleType.class))
                    .plateNumber(Neo4jSupport.stringValue(vehicleNode, "plateNumber"))
                    .build();
        }
    }

    @Override
    public Vehicle save(Vehicle entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "Vehicle", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (v:Vehicle {id: $id})
                        SET v.model = $model,
                            v.name = $model,
                            v.displayName = $model,
                            v.type = $type,
                            v.plateNumber = $plateNumber
                        """, Neo4jSupport.props(
                        "id", id,
                        "model", entity.getModel(),
                        "type", entity.getType() == null ? null : entity.getType().name(),
                        "plateNumber", entity.getPlateNumber()
                ));
                if (entity.getGarage() != null && entity.getGarage().getId() != null) {
                    tx.run("MATCH (:Garage)-[r:STORES]->(v:Vehicle {id: $id}) DELETE r", Map.of("id", id));
                    tx.run("""
                            MATCH (v:Vehicle {id: $vehicleId})
                            MATCH (g:Garage {id: $garageId})
                            MERGE (g)-[:STORES]->(v)
                            """, Map.of("garageId", entity.getGarage().getId(), "vehicleId", id));
                }
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public Vehicle update(Vehicle entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (v:Vehicle {id: $id}) DETACH DELETE v", Map.of("id", id));
                return null;
            });
        }
    }
}
