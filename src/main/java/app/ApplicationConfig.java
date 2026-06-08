package app;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.net.ServerSocket;

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

    public int start(int portNumber, EndpointGroup routes) {
        app.routes(routes);
        int availablePort = findAvailablePort(portNumber);
        app.start(availablePort);
        return availablePort;
    }

    private int findAvailablePort(int preferredPort) {
        int maxPort = 65535;
        for (int port = preferredPort; port <= maxPort; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new IllegalStateException("No available port found from " + preferredPort + " to " + maxPort);
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndRegisterModules();
    }

}
