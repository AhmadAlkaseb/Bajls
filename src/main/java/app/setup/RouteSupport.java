package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.JpaReadDao;
import app.dao.ReadRepository;
import app.dao.WriteRepository;
import jakarta.persistence.EntityManagerFactory;
import persistence.enums.ProfileRole;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public final class RouteSupport {

    private RouteSupport() {
    }

    public static <R, W> CrudController<R, W> jpaCrudController(
            EntityManagerFactory entityManagerFactory,
            ReadQuery<R> query,
            WriteRepository<W> writeRepository,
            Class<W> entityClass
    ) {
        JpaReadDao<R> readRepository = new JpaReadDao<>(
                entityManagerFactory,
                query.getListJpql(),
                query.getByIdJpql(),
                query.getDtoClass()
        );
        return new CrudController<>(readRepository, writeRepository, entityClass);
    }

    public static <R, W> void addAuthenticatedCrudRoutes(
            String resourcePath,
            AuthService authService,
            CrudController<R, W> controller
    ) {
        path(resourcePath, () -> {
            before(ctx -> authService.requireAuthenticated(ctx));
            addCrudHandlers(controller);
        });
    }

    public static <R, W> void addAdminCrudRoutes(
            String resourcePath,
            AuthService authService,
            CrudController<R, W> controller
    ) {
        path(resourcePath, () -> {
            before(ctx -> authService.requireRole(ctx, ProfileRole.ADMIN));
            addCrudHandlers(controller);
        });
    }

    public static <R, W> void addProfileRoutes(AuthService authService, CrudController<R, W> controller) {
        path("profiles", () -> {
            before(ctx -> authService.requireRole(ctx, ProfileRole.ADMIN));
            get(ctx -> controller.getAll(ctx));
            post(ctx -> controller.create(ctx));

            path("{id}", () -> {
                before(ctx -> authService.requireProfileOwnerOrAdmin(ctx));
                get(ctx -> controller.getById(ctx));
                put(ctx -> controller.update(ctx));
                delete(ctx -> controller.delete(ctx));
            });
        });
    }

    public static <R, W> void addCrudHandlers(CrudController<R, W> controller) {
        get(ctx -> controller.getAll(ctx));
        get("{id}", ctx -> controller.getById(ctx));
        post(ctx -> controller.create(ctx));
        put("{id}", ctx -> controller.update(ctx));
        delete(ctx -> controller.delete(ctx));
    }
}
