package app.setup;

import app.route.AppRoutes;

public final class PersistenceBootstrap {

    private PersistenceBootstrap() {
    }

    public static AppBackend createAppBackend(DatabaseType databaseType, boolean isTest) {
        return switch (databaseType) {
            case POSTGRES -> new SimpleAppBackend(new JpaAppPersistence(isTest), persistence -> AppRoutes.build(persistence));
            case MONGODB -> new SimpleAppBackend(new MongoAppPersistence(), persistence -> AppRoutes.build(persistence));
            case NEO4J -> new SimpleAppBackend(new Neo4jAppPersistence(), persistence -> AppRoutes.build(persistence));
        };
    }
}
