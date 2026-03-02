package app;

import app.route.Routes;
import jakarta.persistence.EntityManagerFactory;
import persistence.HibernateConfig;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

        ApplicationConfig.getInstance()
                .setRoute(Routes.getRoutes(emf))
                .startServer(port);
    }
}
