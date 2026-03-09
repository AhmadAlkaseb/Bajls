package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GangAffiliationDTO {
    private Long id;
    private Long characterId;
    private Long gangId;
    private LocalDate joinDate;
}
