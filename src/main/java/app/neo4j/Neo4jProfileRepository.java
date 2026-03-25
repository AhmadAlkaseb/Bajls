package app.neo4j;

import app.dao.ProfileEntityRepository;
import app.dto.LoginResponseDTO;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import persistence.entity.Profile;
import persistence.enums.ProfileRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jProfileRepository implements ProfileEntityRepository {
    private final Driver driver;
    private final Neo4jSequenceRepository sequenceRepository;

    public Neo4jProfileRepository(Driver driver, Neo4jSequenceRepository sequenceRepository) {
        this.driver = driver;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public List<Profile> findAll() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run(
                    "MATCH (p:Profile) RETURN p ORDER BY p.id"
            ).list(record -> toProfile(record.get("p").asNode())));
        }
    }

    @Override
    public Profile findById(Long id) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run(
                    "MATCH (p:Profile {id: $id}) RETURN p",
                    Map.of("id", id)
            ).list());
            Record record = records.isEmpty() ? null : records.get(0);
            return record == null ? null : toProfile(record.get("p").asNode());
        }
    }

    @Override
    public Profile save(Profile entity) {
        Long id = Neo4jSupport.ensureEntityId(sequenceRepository, "Profile", entity);
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MERGE (p:Profile {id: $id})
                        SET p.firstName = $firstName,
                            p.lastName = $lastName,
                            p.email = $email,
                            p.username = $username,
                            p.password = $password,
                            p.role = $role
                        """, Neo4jSupport.props(
                        "id", id,
                        "firstName", entity.getFirstName(),
                        "lastName", entity.getLastName(),
                        "email", entity.getEmail(),
                        "username", entity.getUsername(),
                        "password", entity.getPassword(),
                        "role", entity.getRole() == null ? null : entity.getRole().name()
                ));
                return null;
            });
        }
        return findById(id);
    }

    @Override
    public Profile update(Profile entity) {
        return save(entity);
    }

    @Override
    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (p:Profile {id: $id}) DETACH DELETE p", Map.of("id", id));
                return null;
            });
        }
    }

    @Override
    public LoginResponseDTO authenticate(String username, String password) {
        try (Session session = driver.session()) {
            List<Record> records = session.executeRead(tx -> tx.run("""
                    MATCH (p:Profile {username: $username, password: $password})
                    RETURN p
                    """, Map.of("username", username, "password", password)).list());
            Record record = records.isEmpty() ? null : records.get(0);
            if (record == null) {
                return null;
            }
            Profile profile = toProfile(record.get("p").asNode());
            return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
        }
    }

    private Profile toProfile(Node node) {
        return Profile.builder()
                .id(Neo4jSupport.longValue(node, "id"))
                .firstName(Neo4jSupport.stringValue(node, "firstName"))
                .lastName(Neo4jSupport.stringValue(node, "lastName"))
                .email(Neo4jSupport.stringValue(node, "email"))
                .username(Neo4jSupport.stringValue(node, "username"))
                .password(Neo4jSupport.stringValue(node, "password"))
                .role(Neo4jSupport.enumValue(node, "role", ProfileRole.class))
                .characters(new ArrayList<>())
                .build();
    }
}
