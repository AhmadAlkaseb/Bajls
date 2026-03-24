package app.setup;

import io.javalin.apibuilder.EndpointGroup;

import java.util.function.Function;

public final class SimpleAppBackend implements AppBackend {
    private final AppPersistence persistence;
    private final Function<AppPersistence, EndpointGroup> routesBuilder;

    public SimpleAppBackend(AppPersistence persistence, Function<AppPersistence, EndpointGroup> routesBuilder) {
        this.persistence = persistence;
        this.routesBuilder = routesBuilder;
    }

    @Override
    public EndpointGroup routes() {
        return routesBuilder.apply(persistence);
    }

    @Override
    public void close() {
        persistence.close();
    }
}
