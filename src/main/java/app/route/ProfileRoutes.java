package app.route;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.JpaReadDao;
import app.dao.ProfileDao;
import app.dto.ProfileDTO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;
import persistence.enums.ProfileRole;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class ProfileRoutes {

    private ProfileRoutes() {
    }

    public static EndpointGroup routes(EntityManagerFactory entityManagerFactory, AuthService authService) {
        CrudController<ProfileDTO, Profile> controller = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role) FROM Profile p",
                        "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role) FROM Profile p WHERE p.id = :id",
                        ProfileDTO.class
                ),
                new ProfileDao(entityManagerFactory),
                Profile.class
        );

        return () -> path("profiles", () -> {
            before(ctx -> authService.requireRole(ctx, ProfileRole.ADMIN));
            get(controller::getAll);
            post(controller::create);

            path("{id}", () -> {
                before(authService::requireProfileOwnerOrAdmin);
                get(controller::getById);
                put(controller::update);
                delete(controller::delete);
            });
        });
    }
}
