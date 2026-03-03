package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.RoleDTO;
import jakarta.persistence.EntityManagerFactory;

public class RoleReadDao extends JpaReadDao<RoleDTO> {
    public RoleReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r",
                "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r WHERE r.id = :id",
                RoleDTO.class
        );
    }
}
