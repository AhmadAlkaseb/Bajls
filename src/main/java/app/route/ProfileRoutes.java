package app.route;

import app.auth.AuthService;
import app.setup.AppPersistence;
import app.setup.RouteSupport;
import io.javalin.apibuilder.EndpointGroup;

public class ProfileRoutes {

    private ProfileRoutes() {
    }

    public static EndpointGroup routes(AppPersistence persistence, AuthService authService) {
        return () -> RouteSupport.addProfileRoutes(authService, persistence.profileController());
    }
}
