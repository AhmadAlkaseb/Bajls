package app;

import app.migration.PostgresMigration;
import app.migration.PostgresReset;
import app.route.AppRoutes;
import app.setup.AppPersistence;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;

public class Main {
    private static final int POSTGRES_PORT = 7075;
    private static final int MONGODB_PORT = 7076;
    private static final int NEO4J_PORT = 7077;

    public static void main(String[] args) {
        PostgresReset.resetAndSeed(false);
        PostgresMigration.migrateAll(false);

        AppPersistence postgresPersistence = PersistenceBootstrap.createPersistence(DatabaseType.POSTGRES, false);
        AppPersistence mongoPersistence = PersistenceBootstrap.createPersistence(DatabaseType.MONGODB, false);
        AppPersistence neo4jPersistence = PersistenceBootstrap.createPersistence(DatabaseType.NEO4J, false);

        int postgresPort = new ApplicationConfig().start(POSTGRES_PORT, AppRoutes.build(postgresPersistence));
        int mongoPort = new ApplicationConfig().start(MONGODB_PORT, AppRoutes.build(mongoPersistence));
        int neo4jPort = new ApplicationConfig().start(NEO4J_PORT, AppRoutes.build(neo4jPersistence));

        System.out.println("PostgreSQL API: http://localhost:" + postgresPort + "/api");
        System.out.println("MongoDB API:    http://localhost:" + mongoPort + "/api");
        System.out.println("Neo4j API:     http://localhost:" + neo4jPort + "/api");
    }
}
