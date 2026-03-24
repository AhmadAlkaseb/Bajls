package app;

import app.migration.PostgresMigration;
import app.migration.StartupMessages;
import app.setup.AppBackend;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;

public class Main {
    private static final int DEFAULT_PORT = 7070;
    private static final boolean RUN_POSTGRES_MIGRATION_ON_STARTUP = true;
    private static final DatabaseType DATABASE_TYPE = DatabaseType.NEO4J;

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));

        if (RUN_POSTGRES_MIGRATION_ON_STARTUP) {
            PostgresMigration.migrate(DATABASE_TYPE, false);
        }

        AppBackend appBackend = PersistenceBootstrap.createAppBackend(DATABASE_TYPE, false);
        StartupMessages.print(DATABASE_TYPE, port, RUN_POSTGRES_MIGRATION_ON_STARTUP);
        new ApplicationConfig().start(port, appBackend.routes());
    }
}

// Postgres data findes i pgAdmin
// MongoDB data findes i Compass
// Neo4j data findes i Neo4j Browser