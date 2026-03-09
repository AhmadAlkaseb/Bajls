package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long actorProfileId;
    private String actorUsername;
    private String actorRole;
    private String action;
    private String entityName;
    private Long entityId;
    private String requestMethod;
    private String requestPath;
    private String oldValues;
    private String newValues;
    private LocalDateTime changedAt;
}
