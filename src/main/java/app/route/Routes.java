package app.route;

import app.auth.AuthService;
import app.dto.*;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private Routes() {
    }

    public static EndpointGroup getRoutes(EntityManagerFactory emf) {
        AuthService authService = new AuthService(emf);

        return () -> {
            get("/health", ctx -> ctx.json("ok"));

            path("auth", () -> {
                post("login", authService::login);
                post("logout", ctx -> {
                    authService.requireAuthenticated(ctx);
                    authService.logout(ctx);
                });
                get("me", ctx -> {
                    authService.requireAuthenticated(ctx);
                    authService.me(ctx);
                });
            });

            path("roles", () -> {
                before(authService::requireAuthenticated);
                before(authService::requireAdmin);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r", RoleDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r WHERE r.id = :id", RoleDTO.class));
            });

            path("profiles", () -> {
                before(authService::requireAuthenticated);
                before(authService::requireAdmin);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.password, p.role.id) FROM Profile p", ProfileDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.password, p.role.id) FROM Profile p WHERE p.id = :id", ProfileDTO.class));
            });

            path("characters", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c", GameCharacterDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c WHERE c.id = :id", GameCharacterDTO.class));
            });

            path("genders", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g", GenderDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g WHERE g.id = :id", GenderDTO.class));
            });

            path("skin-colors", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s", SkinColorDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s WHERE s.id = :id", SkinColorDTO.class));
            });

            path("eye-colors", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e", EyeColorDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e WHERE e.id = :id", EyeColorDTO.class));
            });

            path("heights", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h", HeightDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h WHERE h.id = :id", HeightDTO.class));
            });

            path("weights", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w", WeightDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w WHERE w.id = :id", WeightDTO.class));
            });

            path("houses", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c", HouseDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c WHERE h.id = :id", HouseDTO.class));
            });

            path("gangs", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g", GangDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g WHERE g.id = :id", GangDTO.class));
            });

            path("gang-affiliations", () -> {
                before(authService::requireAuthenticated);
                get(ctx -> list(ctx, emf, "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga", GangAffiliationDTO.class));
                get("{id}", ctx -> byId(ctx, emf, "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga WHERE ga.id = :id", GangAffiliationDTO.class));
            });
        };
    }

    private static <T> void list(Context ctx, EntityManagerFactory emf, String jpql, Class<T> dtoClass) {
        EntityManager em = emf.createEntityManager();
        try {
            List<T> result = em.createQuery(jpql, dtoClass).getResultList();
            ctx.json(result);
        } finally {
            em.close();
        }
    }

    private static <T> void byId(Context ctx, EntityManagerFactory emf, String jpql, Class<T> dtoClass) {
        Integer id = parseId(ctx);
        if (id == null) {
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            Optional<T> result = em.createQuery(jpql, dtoClass)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();

            if (result.isPresent()) {
                ctx.json(result.get());
            } else {
                ctx.status(404).json("Not found");
            }
        } finally {
            em.close();
        }
    }

    private static Integer parseId(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json("Invalid id");
            return null;
        }
    }
}
