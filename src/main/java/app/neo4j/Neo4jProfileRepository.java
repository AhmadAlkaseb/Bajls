package app.neo4j;

import app.dao.ProfileEntityRepository;
import app.dto.LoginResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import persistence.entity.Profile;

import java.util.List;

public class Neo4jProfileRepository extends Neo4jEntityRepository<Profile> implements ProfileEntityRepository {
    private final Driver driver;
    private final ObjectMapper objectMapper;

    public Neo4jProfileRepository(Driver driver, Neo4jSequenceRepository sequenceRepository, ObjectMapper objectMapper) {
        super(driver, sequenceRepository, Profile.class, objectMapper);
        this.driver = driver;
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginResponseDTO authenticate(String username, String password) {
        try (Session session = driver.session()) {
            List<Profile> profiles = session.executeRead(tx -> tx.run(
                    "MATCH (n:Profile) RETURN n.payload AS payload"
            ).list().stream()
                    .map(row -> Neo4jSupport.toEntity(row, Profile.class, objectMapper))
                    .filter(profile -> profile != null
                            && username.equals(profile.getUsername())
                            && password.equals(profile.getPassword()))
                    .findFirst()
                    .stream()
                    .toList());
            Profile profile = profiles.isEmpty() ? null : profiles.get(0);
            if (profile == null) {
                return null;
            }

            return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
        }
    }
}
