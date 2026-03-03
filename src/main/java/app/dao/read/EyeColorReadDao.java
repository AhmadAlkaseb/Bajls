package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.EyeColorDTO;
import jakarta.persistence.EntityManagerFactory;

public class EyeColorReadDao extends JpaReadDao<EyeColorDTO> {
    public EyeColorReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e",
                "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e WHERE e.id = :id",
                EyeColorDTO.class
        );
    }
}
