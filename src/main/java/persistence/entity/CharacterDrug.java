package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "character_drug",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_character_drug_character_drug",
                columnNames = {"character_id", "drug_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CharacterDrug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_character_drug_character"))
    @ToString.Exclude
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id", nullable = false, foreignKey = @ForeignKey(name = "fk_character_drug_drug"))
    @ToString.Exclude
    private Drug drug;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
