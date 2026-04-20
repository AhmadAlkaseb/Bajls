package app.setup;

import app.controller.CrudController;
import app.dao.EntityRepository;
import app.dao.MappedReadRepository;
import app.dto.ProfileDTO;
import persistence.entity.Profile;

import java.util.function.Function;

public final class PersistenceSupport {

    private PersistenceSupport() {
    }

    public static CrudController<ProfileDTO, Profile> profileController(EntityRepository<Profile> profileRepository) {
        return new CrudController<>(
                new MappedReadRepository<>(profileRepository, profile -> DtoMappers.toProfileDto(profile)),
                profileRepository,
                Profile.class
        );
    }

    public static <D, E> CrudController<D, E> mappedCrudController(
            EntityRepository<E> repository,
            Function<E, D> mapper,
            Class<E> entityClass
    ) {
        return new CrudController<>(
                new MappedReadRepository<>(repository, mapper),
                repository,
                entityClass
        );
    }

}
