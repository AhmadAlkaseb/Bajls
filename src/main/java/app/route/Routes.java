package app.route;

import app.auth.AuthService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private Routes() {
    }

    public static EndpointGroup getRoutes(EntityManagerFactory entityManagerFactory) {
        AuthService authService = new AuthService(entityManagerFactory);

        return () -> {
            get("/health", ctx -> ctx.json("ok"));
            path("auth", AuthRoutes.routes(authService));
            ReadRoutes.routes(entityManagerFactory, authService).addEndpoints();
        };
    }
}
