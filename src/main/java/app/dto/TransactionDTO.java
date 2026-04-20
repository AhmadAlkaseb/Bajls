package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import persistence.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long characterId;
    private TransactionType type;
    private BigDecimal amount;
    private Long drugId;
    private Integer quantity;
    private Long targetCharacterId;
    private String description;
    private LocalDateTime createdAt;
}
