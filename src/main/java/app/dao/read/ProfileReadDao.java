package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.ProfileDTO;
import jakarta.persistence.EntityManagerFactory;

public class ProfileReadDao extends JpaReadDao<ProfileDTO> {
    public ProfileReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role.id) FROM Profile p",
                "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role.id) FROM Profile p WHERE p.id = :id",
                ProfileDTO.class
        );
    }
}
