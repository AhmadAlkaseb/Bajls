package app.dao.read;

import app.dao.JpaReadDao;
import app.dto.HouseDTO;
import jakarta.persistence.EntityManagerFactory;

public class HouseReadDao extends JpaReadDao<HouseDTO> {
    public HouseReadDao(EntityManagerFactory entityManagerFactory) {
        super(
                entityManagerFactory,
                "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c",
                "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c WHERE h.id = :id",
                HouseDTO.class
        );
    }
}
