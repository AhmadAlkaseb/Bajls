package app.audit;

import app.dto.LoginResponseDTO;

public final class AuditContext {

    private static final ThreadLocal<AuditMetadata> CONTEXT = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void startRequest(String method, String path) {
        CONTEXT.set(new AuditMetadata(method, path));
    }

    public static void setAuthenticatedUser(LoginResponseDTO user) {
        AuditMetadata metadata = CONTEXT.get();
        if (metadata == null) {
            metadata = new AuditMetadata("UNKNOWN", "UNKNOWN");
            CONTEXT.set(metadata);
        }
        metadata.setActorProfileId(user.getProfileId());
        metadata.setActorUsername(user.getUsername());
        metadata.setActorRole(user.getRole() == null ? null : user.getRole().name());
    }

    public static AuditMetadata getCurrent() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static final class AuditMetadata {
        private final String requestMethod;
        private final String requestPath;
        private Long actorProfileId;
        private String actorUsername;
        private String actorRole;

        private AuditMetadata(String requestMethod, String requestPath) {
            this.requestMethod = requestMethod;
            this.requestPath = requestPath;
        }

        public String getRequestMethod() {
            return requestMethod;
        }

        public String getRequestPath() {
            return requestPath;
        }

        public Long getActorProfileId() {
            return actorProfileId;
        }

        public void setActorProfileId(Long actorProfileId) {
            this.actorProfileId = actorProfileId;
        }

        public String getActorUsername() {
            return actorUsername;
        }

        public void setActorUsername(String actorUsername) {
            this.actorUsername = actorUsername;
        }

        public String getActorRole() {
            return actorRole;
        }

        public void setActorRole(String actorRole) {
            this.actorRole = actorRole;
        }
    }
}
