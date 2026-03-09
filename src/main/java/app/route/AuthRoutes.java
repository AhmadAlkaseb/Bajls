package app.route;

import app.auth.AuthService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoutes {

    private AuthRoutes() {
    }

    public static EndpointGroup routes(AuthService authService) {
        return () -> path("auth", () -> {
            post("login", authService::login);
            post("register", authService::register);
            post("logout", authService::logout);
        });
    }
}
