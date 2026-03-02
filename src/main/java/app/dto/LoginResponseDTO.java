package app.dto;

import persistence.enums.RoleName;

public record LoginResponseDTO(String token, String expiresAt, int profileId, String username, RoleName role) {
}
