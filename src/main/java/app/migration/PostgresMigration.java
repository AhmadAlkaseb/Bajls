package app.migration;

import app.setup.DatabaseType;

public final class PostgresMigration {
    private PostgresMigration() {
    }

    public static void migrate(DatabaseType targetDatabaseType, boolean isTest) {
        if (targetDatabaseType == DatabaseType.POSTGRES) {
            return;
        }

        MigrationSnapshot snapshot = PostgresSnapshotLoader.load(isTest);
        switch (targetDatabaseType) {
            case MONGODB -> MongoMigration.write(snapshot);
            case NEO4J -> Neo4jMigration.write(snapshot);
            case POSTGRES -> { }
        }
    }
}
