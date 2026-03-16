package app;

import app.audit.AuditContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.json.JavalinJackson;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.plugin.bundled.CorsPluginConfig;

public class ApplicationConfig {
    private final Javalin app;

    public ApplicationConfig() {
        this.app = Javalin.create(config -> {
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .findAndRegisterModules();
            config.http.defaultContentType = "application/json";
            config.routing.contextPath = "/api";
            config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
            config.jsonMapper(new JavalinJackson(objectMapper));
        });
        app.before(ctx -> AuditContext.startRequest(ctx.method().name(), ctx.path()));
        app.after(ctx -> AuditContext.clear());
    }

    public void start(int portNumber, EndpointGroup routes) {
        app.routes(routes);
        app.start(portNumber);
    }
}
