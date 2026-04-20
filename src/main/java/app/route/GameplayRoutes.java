package app.route;

import app.auth.AuthService;
import app.setup.AppPersistence;
import app.setup.RouteSupport;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

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

            path("transactions", () -> {
                before(ctx -> authService.requireAuthenticated(ctx));
                get(ctx -> persistence.transactionController().getAll(ctx));
                get("{id}", ctx -> persistence.transactionController().getById(ctx));
                // POST /transactions/buy-drug  — deducts balance, adds drugs to inventory
                post("buy-drug", ctx -> persistence.transactionController().buyDrug(ctx));
                // POST /transactions/transfer  — moves balance between two characters atomically
                post("transfer", ctx -> persistence.transactionController().transfer(ctx));
            });
        };
    }
}
