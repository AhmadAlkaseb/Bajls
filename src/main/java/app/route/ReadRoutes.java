package app.route;

import app.auth.AuthService;
import app.controller.ReadController;
import app.dao.read.EyeColorReadDao;
import app.dao.read.GameCharacterReadDao;
import app.dao.read.GangAffiliationReadDao;
import app.dao.read.GangReadDao;
import app.dao.read.GenderReadDao;
import app.dao.read.HeightReadDao;
import app.dao.read.HouseReadDao;
import app.dao.read.ProfileReadDao;
import app.dao.read.RoleReadDao;
import app.dao.read.SkinColorReadDao;
import app.dao.read.WeightReadDao;
import app.dto.EyeColorDTO;
import app.dto.GameCharacterDTO;
import app.dto.GangAffiliationDTO;
import app.dto.GangDTO;
import app.dto.GenderDTO;
import app.dto.HeightDTO;
import app.dto.HouseDTO;
import app.dto.ProfileDTO;
import app.dto.RoleDTO;
import app.dto.SkinColorDTO;
import app.dto.WeightDTO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public final class ReadRoutes {

    private ReadRoutes() {
    }

    public static EndpointGroup routes(EntityManagerFactory entityManagerFactory, AuthService authService) {
        ReadController<RoleDTO> roleController = new ReadController<>(new RoleReadDao(entityManagerFactory));
        ReadController<ProfileDTO> profileController = new ReadController<>(new ProfileReadDao(entityManagerFactory));
        ReadController<GameCharacterDTO> characterController = new ReadController<>(new GameCharacterReadDao(entityManagerFactory));
        ReadController<GenderDTO> genderController = new ReadController<>(new GenderReadDao(entityManagerFactory));
        ReadController<SkinColorDTO> skinColorController = new ReadController<>(new SkinColorReadDao(entityManagerFactory));
        ReadController<EyeColorDTO> eyeColorController = new ReadController<>(new EyeColorReadDao(entityManagerFactory));
        ReadController<HeightDTO> heightController = new ReadController<>(new HeightReadDao(entityManagerFactory));
        ReadController<WeightDTO> weightController = new ReadController<>(new WeightReadDao(entityManagerFactory));
        ReadController<HouseDTO> houseController = new ReadController<>(new HouseReadDao(entityManagerFactory));
        ReadController<GangDTO> gangController = new ReadController<>(new GangReadDao(entityManagerFactory));
        ReadController<GangAffiliationDTO> gangAffiliationController = new ReadController<>(new GangAffiliationReadDao(entityManagerFactory));

        return () -> {
            securedReadPath("roles", authService, roleController, true);
            securedReadPath("profiles", authService, profileController, true);
            securedReadPath("characters", authService, characterController, false);
            securedReadPath("genders", authService, genderController, false);
            securedReadPath("skin-colors", authService, skinColorController, false);
            securedReadPath("eye-colors", authService, eyeColorController, false);
            securedReadPath("heights", authService, heightController, false);
            securedReadPath("weights", authService, weightController, false);
            securedReadPath("houses", authService, houseController, false);
            securedReadPath("gangs", authService, gangController, false);
            securedReadPath("gang-affiliations", authService, gangAffiliationController, false);
        };
    }

    private static <T> void securedReadPath(
            String pathName,
            AuthService authService,
            ReadController<T> controller,
            boolean adminOnly
    ) {
        path(pathName, () -> {
            before(authService::requireAuthenticated);
            if (adminOnly) {
                before(authService::requireAdmin);
            }
            get(controller::getAll);
            get("{id}", controller::getById);
        });
    }
}
