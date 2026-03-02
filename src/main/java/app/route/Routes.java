package app.route;

import app.auth.AuthService;
import app.controller.ReadController;
import app.dto.*;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private Routes() {
    }

    public static EndpointGroup getRoutes(EntityManagerFactory emf) {
        AuthService authService = new AuthService(emf);

        ReadController<RoleDTO> roleController = new ReadController<>(
                emf,
                "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r",
                "SELECT new app.dto.RoleDTO(r.id, r.name) FROM Role r WHERE r.id = :id",
                RoleDTO.class
        );

        ReadController<ProfileDTO> profileController = new ReadController<>(
                emf,
                "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role.id) FROM Profile p",
                "SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role.id) FROM Profile p WHERE p.id = :id",
                ProfileDTO.class
        );

        ReadController<GameCharacterDTO> characterController = new ReadController<>(
                emf,
                "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c",
                "SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, c.gender.id, c.skinColor.id, c.eyeColor.id, c.height.id, c.weight.id, c.house.id) FROM GameCharacter c WHERE c.id = :id",
                GameCharacterDTO.class
        );

        ReadController<GenderDTO> genderController = new ReadController<>(
                emf,
                "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g",
                "SELECT new app.dto.GenderDTO(g.id, g.name) FROM Gender g WHERE g.id = :id",
                GenderDTO.class
        );

        ReadController<SkinColorDTO> skinColorController = new ReadController<>(
                emf,
                "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s",
                "SELECT new app.dto.SkinColorDTO(s.id, s.name) FROM SkinColor s WHERE s.id = :id",
                SkinColorDTO.class
        );

        ReadController<EyeColorDTO> eyeColorController = new ReadController<>(
                emf,
                "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e",
                "SELECT new app.dto.EyeColorDTO(e.id, e.name) FROM EyeColor e WHERE e.id = :id",
                EyeColorDTO.class
        );

        ReadController<HeightDTO> heightController = new ReadController<>(
                emf,
                "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h",
                "SELECT new app.dto.HeightDTO(h.id, h.name) FROM Height h WHERE h.id = :id",
                HeightDTO.class
        );

        ReadController<WeightDTO> weightController = new ReadController<>(
                emf,
                "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w",
                "SELECT new app.dto.WeightDTO(w.id, w.name) FROM Weight w WHERE w.id = :id",
                WeightDTO.class
        );

        ReadController<HouseDTO> houseController = new ReadController<>(
                emf,
                "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c",
                "SELECT new app.dto.HouseDTO(h.id, h.amountRooms, h.amountBathrooms, c.id) FROM House h LEFT JOIN h.character c WHERE h.id = :id",
                HouseDTO.class
        );

        ReadController<GangDTO> gangController = new ReadController<>(
                emf,
                "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g",
                "SELECT new app.dto.GangDTO(g.id, g.name, g.type) FROM Gang g WHERE g.id = :id",
                GangDTO.class
        );

        ReadController<GangAffiliationDTO> gangAffiliationController = new ReadController<>(
                emf,
                "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga",
                "SELECT new app.dto.GangAffiliationDTO(ga.id, ga.character.id, ga.gang.id, ga.joinDate) FROM GangAffiliation ga WHERE ga.id = :id",
                GangAffiliationDTO.class
        );

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
                get(roleController::getAll);
                get("{id}", roleController::getById);
            });

            path("profiles", () -> {
                before(authService::requireAuthenticated);
                before(authService::requireAdmin);
                get(profileController::getAll);
                get("{id}", profileController::getById);
            });

            path("characters", () -> {
                before(authService::requireAuthenticated);
                get(characterController::getAll);
                get("{id}", characterController::getById);
            });

            path("genders", () -> {
                before(authService::requireAuthenticated);
                get(genderController::getAll);
                get("{id}", genderController::getById);
            });

            path("skin-colors", () -> {
                before(authService::requireAuthenticated);
                get(skinColorController::getAll);
                get("{id}", skinColorController::getById);
            });

            path("eye-colors", () -> {
                before(authService::requireAuthenticated);
                get(eyeColorController::getAll);
                get("{id}", eyeColorController::getById);
            });

            path("heights", () -> {
                before(authService::requireAuthenticated);
                get(heightController::getAll);
                get("{id}", heightController::getById);
            });

            path("weights", () -> {
                before(authService::requireAuthenticated);
                get(weightController::getAll);
                get("{id}", weightController::getById);
            });

            path("houses", () -> {
                before(authService::requireAuthenticated);
                get(houseController::getAll);
                get("{id}", houseController::getById);
            });

            path("gangs", () -> {
                before(authService::requireAuthenticated);
                get(gangController::getAll);
                get("{id}", gangController::getById);
            });

            path("gang-affiliations", () -> {
                before(authService::requireAuthenticated);
                get(gangAffiliationController::getAll);
                get("{id}", gangAffiliationController::getById);
            });
        };
    }
}
