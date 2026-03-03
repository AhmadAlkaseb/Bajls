package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.GenderDTO;
import jakarta.persistence.EntityManagerFactory;

public class GenderReadDao extends JpaReadDao<GenderDTO> {
    public GenderReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g",
                "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g WHERE g.id = :id",
                GenderDTO.class
        );
    }
}
