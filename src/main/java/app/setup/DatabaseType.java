package app.setup;

public enum DatabaseType {
    POSTGRES,
    MONGODB,
    NEO4J;

    public static DatabaseType from(String value) {
        if (value == null || value.isBlank()) {
            return POSTGRES;
        }

        return switch (value.trim().toLowerCase()) {
            case "postgres", "postgresql" -> POSTGRES;
            case "mongodb", "mongo" -> MONGODB;
            case "neo4j" -> NEO4J;
            default -> throw new IllegalArgumentException(
                    "Unsupported database type: " + value + ". Use postgres, mongodb, or neo4j."
            );
        };
    }
}
