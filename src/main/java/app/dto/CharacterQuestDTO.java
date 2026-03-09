package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import persistence.enums.QuestStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterQuestDTO {
    private Long id;
    private Long characterId;
    private Long questId;
    private QuestStatus status;
    private LocalDateTime acceptedAt;
}
