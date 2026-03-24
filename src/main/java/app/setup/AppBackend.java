package app.setup;

import io.javalin.apibuilder.EndpointGroup;

public interface AppBackend extends AutoCloseable {
    EndpointGroup routes();

    @Override
    default void close() {
    }
}
