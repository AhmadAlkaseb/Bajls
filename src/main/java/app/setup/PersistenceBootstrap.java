package app.setup;

public final class PersistenceBootstrap {

    private PersistenceBootstrap() {
    }

    public static AppPersistence createPersistence(DatabaseType databaseType, boolean isTest) {
        return switch (databaseType) {
            case POSTGRES -> new JpaAppPersistence(isTest);
            case MONGODB -> new MongoAppPersistence();
            case NEO4J -> new Neo4jAppPersistence();
        };
    }
}
