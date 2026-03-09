package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import persistence.enums.EyeColorType;
import persistence.enums.GenderType;
import persistence.enums.SkinColorType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameCharacterDTO {
    private Long id;
    private String name;
    private BigDecimal balance;
    private Long profileId;
    private GenderType gender;
    private SkinColorType skincolor;
    private EyeColorType eyecolor;
    private String height;
    private String weight;
    private Long houseId;
    private Long garageId;
}
