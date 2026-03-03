package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.HeightDTO;
import jakarta.persistence.EntityManagerFactory;

public class HeightReadDao extends JpaReadDao<HeightDTO> {
    public HeightReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h",
                "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h WHERE h.id = :id",
                HeightDTO.class
        );
    }
}
