package app.dao;

import app.auth.AuthPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;

import java.util.Optional;

public class ProfileDao extends AbstractJpaDao<Profile> {
    public ProfileDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Profile.class);
    }

    public Optional<AuthPrincipal> authenticate(String username, String password) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT new app.auth.AuthPrincipal(p.id, p.username, p.role.name) " +
                                    "FROM Profile p " +
                                    "WHERE p.username = :username AND p.password = :password",
                            AuthPrincipal.class
                    )
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
