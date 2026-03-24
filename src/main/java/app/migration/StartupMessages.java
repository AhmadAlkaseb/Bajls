package app.migration;

import app.setup.DatabaseType;

public final class StartupMessages {
    private StartupMessages() {
    }

    public static void print(DatabaseType databaseType, int port, boolean migrationEnabled) {
        System.out.println("Starting application with database: " + databaseType);
        System.out.println("HTTP API base URL: http://localhost:" + port + "/api");
        if (migrationEnabled && databaseType != DatabaseType.POSTGRES) {
            System.out.println("Postgres migration is enabled for target: " + databaseType);
        }
    }
}
