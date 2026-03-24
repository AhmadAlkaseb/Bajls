package app.dao;

import app.dto.LoginResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;

public class ProfileDao extends AbstractJpaDao<Profile> implements ProfileEntityRepository {
    public ProfileDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Profile.class);
    }

    public LoginResponseDTO authenticate(String username, String password) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                            "SELECT new app.dto.LoginResponseDTO(p.id, p.username, p.role) " +
                                    "FROM Profile p " +
                                    "WHERE p.username = :username AND p.password = :password",
                            LoginResponseDTO.class
                    )
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}
