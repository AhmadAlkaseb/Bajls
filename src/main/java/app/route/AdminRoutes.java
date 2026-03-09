package app.route;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.DrugDao;
import app.dao.JpaReadDao;
import app.dao.QuestDao;
import app.dto.DrugDTO;
import app.dto.QuestDTO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Drug;
import persistence.entity.Quest;
import persistence.enums.ProfileRole;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class AdminRoutes {

    private AdminRoutes() {
    }

    public static EndpointGroup routes(EntityManagerFactory entityManagerFactory, AuthService authService) {
        CrudController<DrugDTO, Drug> drugController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.DrugDTO(d.id, d.name, d.type) FROM Drug d",
                        "SELECT new app.dto.DrugDTO(d.id, d.name, d.type) FROM Drug d WHERE d.id = :id",
                        DrugDTO.class
                ),
                new DrugDao(entityManagerFactory),
                Drug.class
        );

        CrudController<QuestDTO, Quest> questController = new CrudController<>(
                new JpaReadDao<>(
                        entityManagerFactory,
                        "SELECT new app.dto.QuestDTO(q.id, q.title, q.description, q.reward) FROM Quest q",
                        "SELECT new app.dto.QuestDTO(q.id, q.title, q.description, q.reward) FROM Quest q WHERE q.id = :id",
                        QuestDTO.class
                ),
                new QuestDao(entityManagerFactory),
                Quest.class
        );

        return () -> {
            path("drugs", () -> {
                before(ctx -> authService.requireRole(ctx, ProfileRole.ADMIN));
                get(drugController::getAll);
                get("{id}", drugController::getById);
                post(drugController::create);
                put("{id}", drugController::update);
                delete("{id}", drugController::delete);
            });

            path("quests", () -> {
                before(ctx -> authService.requireRole(ctx, ProfileRole.ADMIN));
                get(questController::getAll);
                get("{id}", questController::getById);
                post(questController::create);
                put("{id}", questController::update);
                delete("{id}", questController::delete);
            });
        };
    }
}
