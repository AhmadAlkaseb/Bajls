package app;

import app.audit.AuditSchemaInitializer;
import app.route.Routes;
import jakarta.persistence.EntityManagerFactory;
import persistence.HibernateConfig;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
        AuditSchemaInitializer.initialize(emf);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        new ApplicationConfig().start(port, Routes.getRoutes(emf));
    }
}
