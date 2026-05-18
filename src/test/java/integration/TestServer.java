package integration;

import app.ApplicationConfig;
import app.route.AppRoutes;
import app.setup.AppPersistence;
import io.javalin.apibuilder.EndpointGroup;

import java.net.ServerSocket;

final class TestServer {

    private TestServer() {}

    static int start(AppPersistence persistence) {
        int port = findFreePort();
        EndpointGroup routes = AppRoutes.build(persistence);
        new ApplicationConfig().start(port, routes);
        return port;
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("Could not find a free port", e);
        }
    }
}