package app.route;

import app.auth.AuthService;
import app.setup.AppPersistence;
import app.setup.RouteSupport;
import io.javalin.apibuilder.EndpointGroup;

public class AdminRoutes {

    private AdminRoutes() {
    }

    public static EndpointGroup routes(AppPersistence persistence, AuthService authService) {
        return () -> {
            RouteSupport.addAdminCrudRoutes("drugs", authService, persistence.drugController());
            RouteSupport.addAdminCrudRoutes("quests", authService, persistence.questController());
        };
    }
}
