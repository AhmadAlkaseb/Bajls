package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "gang_affiliations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_gang_affiliations_character_gang",
                columnNames = {"character_id", "gang_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GangAffiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gang_aff_char"))
    @ToString.Exclude
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gang_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gang_aff_gang"))
    @ToString.Exclude
    private Gang gang;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;
}
