package app.migration;

import persistence.HibernateConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class PostgresReset {
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/bajls";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final Path SEED_FILE = Path.of("sqls", "seed.sql");

    private PostgresReset() {
    }

    public static void resetAndSeed(boolean isTest) {
        if (isTest) {
            return;
        }

        recreateSchema();
        HibernateConfig.getEntityManagerFactoryConfig(false);
        runSeedScript();
    }

    private static void recreateSchema() {
        try (Connection connection = openConnection()) {
            executeStatement(connection, "DROP SCHEMA IF EXISTS public CASCADE");
            executeStatement(connection, "CREATE SCHEMA public");
            executeStatement(connection, "GRANT ALL ON SCHEMA public TO public");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to recreate PostgreSQL schema", e);
        }
    }

    private static void runSeedScript() {
        try (Connection connection = openConnection()) {
            String sql = Files.readString(SEED_FILE);
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (trimmed.isBlank()) {
                    continue;
                }
                executeStatement(connection, trimmed);
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to run PostgreSQL seed script", e);
        }
    }

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                envOrDefault("DB_URL", DEFAULT_DB_URL),
                envOrDefault("DB_USER", DEFAULT_DB_USER),
                envOrDefault("DB_PASSWORD", DEFAULT_DB_PASSWORD)
        );
    }

    private static void executeStatement(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
