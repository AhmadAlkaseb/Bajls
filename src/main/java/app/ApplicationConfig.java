package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.plugin.bundled.CorsPluginConfig;

public class ApplicationConfig {
    private final Javalin app;

    public ApplicationConfig() {
        this.app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.routing.contextPath = "/api";
            config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
        });
    }

    public void start(int portNumber, EndpointGroup routes) {
        app.routes(routes);
        app.start(portNumber);
    }
}
