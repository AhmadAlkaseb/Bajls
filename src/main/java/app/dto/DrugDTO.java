package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import persistence.enums.DrugType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrugDTO {
    private Long id;
    private String name;
    private DrugType type;
}
