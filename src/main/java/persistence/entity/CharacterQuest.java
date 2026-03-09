package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import persistence.enums.QuestStatus;

@Entity
@Table(
        name = "character_quest",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_character_quest_character_quest",
                columnNames = {"character_id", "quest_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CharacterQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_character_quest_character"))
    @ToString.Exclude
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false, foreignKey = @ForeignKey(name = "fk_character_quest_quest"))
    @ToString.Exclude
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuestStatus status;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;
}
