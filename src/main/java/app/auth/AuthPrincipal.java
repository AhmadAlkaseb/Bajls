package app.auth;

import persistence.enums.RoleName;

public record AuthPrincipal(int profileId, String username, RoleName roleName) {
}
