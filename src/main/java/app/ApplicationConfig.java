package app;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.json.JavalinJackson;

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

}
