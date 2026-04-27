package app.dao;

import app.auth.PasswordSupport;
import app.dto.LoginResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;

public class ProfileDao extends JpaDao<Profile> implements ProfileEntityRepository {
    public ProfileDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Profile.class);
    }

    public LoginResponseDTO authenticate(String username, String password) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            Profile profile = em.createQuery(
                            "SELECT p FROM Profile p WHERE p.username = :username",
                            Profile.class
                    )
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (profile == null || !PasswordSupport.matches(password, profile.getPassword())) {
                return null;
            }

            return new LoginResponseDTO(profile.getId(), profile.getUsername(), profile.getRole());
        }
    }
}
