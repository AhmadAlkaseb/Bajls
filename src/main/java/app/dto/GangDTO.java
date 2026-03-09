package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import persistence.enums.GangType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GangDTO {
    private Long id;
    private String name;
    private GangType type;
}
