package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyDrugRequest {
    private Long characterId;
    private Long drugId;
    private int quantity;
    private BigDecimal pricePerUnit;
}
