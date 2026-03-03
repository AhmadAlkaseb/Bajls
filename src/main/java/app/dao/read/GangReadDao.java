package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.GangDTO;
import jakarta.persistence.EntityManagerFactory;

public class GangReadDao extends JpaReadDao<GangDTO> {
    public GangReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g",
                "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g WHERE g.id = :id",
                GangDTO.class
        );
    }
}
