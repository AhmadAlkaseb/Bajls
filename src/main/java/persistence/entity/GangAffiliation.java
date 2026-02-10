package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gang_affiliations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GangAffiliation {

    @EmbeddedId
    private GangAffiliationId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("characterId")
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gang_aff_char"))
    @ToString.Exclude
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("gangId")
    @JoinColumn(name = "gang_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gang_aff_gang"))
    @ToString.Exclude
    private Gang gang;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;
}
