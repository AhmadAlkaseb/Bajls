package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.SkinColorDTO;
import jakarta.persistence.EntityManagerFactory;

public class SkinColorReadDao extends JpaReadDao<SkinColorDTO> {
    public SkinColorReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s",
                "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s WHERE s.id = :id",
                SkinColorDTO.class
        );
    }
}
