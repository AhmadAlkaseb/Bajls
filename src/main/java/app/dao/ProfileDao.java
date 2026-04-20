package app.dao;

import app.dto.LoginResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mindrot.jbcrypt.BCrypt;
import persistence.entity.Profile;

public class ProfileDao extends JpaDao<Profile> implements ProfileEntityRepository {
    public ProfileDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Profile.class);
    }

    public LoginResponseDTO authenticate(String username, String password) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            // Fetch the stored hash by username only, then verify the password
            // in Java. Never compare passwords in the query — hashed passwords
            // cannot be matched with SQL equality.
            Profile profile = em.createQuery(
                            "SELECT p FROM Profile p WHERE p.username = :username",
                            Profile.class
                    )
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            // Password verification logic should be added here
            if (profile != null && profile.getPassword().equals(password)) {
                return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
            }
            return null;
        }
    }

            if (profile == null || !BCrypt.checkpw(password, profile.getPassword())) {
                return null;
            }
            return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
        }
    }
}
