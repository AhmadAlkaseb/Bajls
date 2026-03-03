package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.WeightDTO;
import jakarta.persistence.EntityManagerFactory;

public class WeightReadDao extends JpaReadDao<WeightDTO> {
    public WeightReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w",
                "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w WHERE w.id = :id",
                WeightDTO.class
        );
    }
}
