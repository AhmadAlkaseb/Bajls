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
    private static final String API_CONTEXT_PATH = "/api";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private final Javalin app;

    public ApplicationConfig() {
        this.app = Javalin.create(config -> {
            config.http.defaultContentType = DEFAULT_CONTENT_TYPE;
            config.routing.contextPath = API_CONTEXT_PATH;
            config.plugins.enableCors(cors -> cors.add(rule -> rule.anyHost()));
            config.jsonMapper(new JavalinJackson(createObjectMapper()));
        });
        configureRequestLifecycle();
    }

    public void start(int portNumber, EndpointGroup routes) {
        app.routes(routes);
        app.start(portNumber);
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndRegisterModules();
    }

    private void configureRequestLifecycle() {
        app.before(ctx -> AuditContext.startRequest(ctx.method().name(), ctx.path()));
        app.after(ctx -> AuditContext.clear());
    }
}
