package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.GangAffiliationDTO;
import jakarta.persistence.EntityManagerFactory;

public class GangAffiliationReadDao extends JpaReadDao<GangAffiliationDTO> {
    public GangAffiliationReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga",
                "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga WHERE ga.id = :id",
                GangAffiliationDTO.class
        );
    }
}
