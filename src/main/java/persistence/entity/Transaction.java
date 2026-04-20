package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_character_id", columnList = "character_id"),
                @Index(name = "idx_transactions_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transactions_character"))
    @ToString.Exclude
    private GameCharacter character;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    // Negative = money spent, Positive = money received
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", foreignKey = @ForeignKey(name = "fk_transactions_drug"))
    @ToString.Exclude
    private Drug drug;

    @Column(name = "quantity")
    private Integer quantity;

    // For TRANSFER: the other character in the transaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_character_id", foreignKey = @ForeignKey(name = "fk_transactions_target_character"))
    @ToString.Exclude
    private GameCharacter targetCharacter;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
