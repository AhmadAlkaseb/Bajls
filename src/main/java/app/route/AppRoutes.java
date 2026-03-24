package app.route;

import app.auth.AuthService;
import app.setup.AppPersistence;
import io.javalin.apibuilder.EndpointGroup;

public final class AppRoutes {

    private AppRoutes() {
    }

    public static EndpointGroup build(AppPersistence persistence) {
        AuthService authService = persistence.authService();

        return () -> {
            AuthRoutes.routes(authService).addEndpoints();
            ProfileRoutes.routes(persistence, authService).addEndpoints();
            AdminRoutes.routes(persistence, authService).addEndpoints();
            GameplayRoutes.routes(persistence, authService).addEndpoints();
        };
    }
}
