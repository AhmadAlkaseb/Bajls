package app.route;

import app.auth.AuthService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public final class AuthRoutes {

    private AuthRoutes() {
    }

    public static EndpointGroup routes(AuthService authService) {
        return () -> {
            post("login", authService::login);
            post("logout", ctx -> {
                authService.requireAuthenticated(ctx);
                authService.logout(ctx);
            });
            get("me", ctx -> {
                authService.requireAuthenticated(ctx);
                authService.me(ctx);
            });
        };
    }
}
