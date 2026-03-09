package app.route;

import app.auth.AuthService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class Routes {

    private Routes() {
    }

    public static EndpointGroup getRoutes(EntityManagerFactory entityManagerFactory) {
        AuthService authService = new AuthService(entityManagerFactory);

        return () -> {
            AuthRoutes.routes(authService).addEndpoints();
            ProfileRoutes.routes(entityManagerFactory, authService).addEndpoints();
            AdminRoutes.routes(entityManagerFactory, authService).addEndpoints();
            GameplayRoutes.routes(entityManagerFactory, authService).addEndpoints();
        };
    }
}
