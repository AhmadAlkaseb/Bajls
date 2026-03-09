package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HouseDTO {
    private Long id;
    private int amountRooms;
    private int amountBathrooms;
    private Long characterId;
}
