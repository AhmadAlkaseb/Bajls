package app;

import app.migration.PostgresMigration;
import app.route.AppRoutes;
import app.setup.AppPersistence;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;

public class Main {
    private static final int DEFAULT_PORT = 7070;
    private static final boolean RUN_POSTGRES_MIGRATION_ON_STARTUP = true;
    private static final DatabaseType DATABASE_TYPE = DatabaseType.POSTGRES;

    public static void main(String[] args) {
        if (RUN_POSTGRES_MIGRATION_ON_STARTUP) {
            PostgresMigration.migrate(DATABASE_TYPE, false);
        }

        AppPersistence persistence = PersistenceBootstrap.createPersistence(DATABASE_TYPE, false);
        new ApplicationConfig().start(PORT, AppRoutes.build(persistence));
    }
}
