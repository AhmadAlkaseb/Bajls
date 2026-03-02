package app.dto;
import java.time.LocalDate;
public record GangAffiliationDTO(int id, int characterId, int gangId, LocalDate joinDate) {
}
