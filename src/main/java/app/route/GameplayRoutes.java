package app.route;

import app.auth.AuthService;
import app.setup.AppPersistence;
import app.setup.RouteSupport;
import io.javalin.apibuilder.EndpointGroup;

public class GameplayRoutes {

    private GameplayRoutes() {
    }

    public static EndpointGroup routes(AppPersistence persistence, AuthService authService) {
        return () -> {
            RouteSupport.addAuthenticatedCrudRoutes("characters", authService, persistence.characterController());
            RouteSupport.addAuthenticatedCrudRoutes("houses", authService, persistence.houseController());
            RouteSupport.addAuthenticatedCrudRoutes("garages", authService, persistence.garageController());
            RouteSupport.addAuthenticatedCrudRoutes("vehicles", authService, persistence.vehicleController());
            RouteSupport.addAuthenticatedCrudRoutes("character-drug", authService, persistence.characterDrugController());
            RouteSupport.addAuthenticatedCrudRoutes("character-quest", authService, persistence.characterQuestController());
            RouteSupport.addAuthenticatedCrudRoutes("gangs", authService, persistence.gangController());
            RouteSupport.addAuthenticatedCrudRoutes("gang-affiliations", authService, persistence.gangAffiliationController());
        };
    }
}
