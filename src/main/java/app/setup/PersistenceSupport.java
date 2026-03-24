package app.setup;

import app.auth.AuthService;
import app.controller.CrudController;
import app.dao.EntityRepository;
import app.dao.MappedReadRepository;
import app.dao.ReadRepository;
import app.dto.AuditLogDTO;
import app.dto.ProfileDTO;
import persistence.entity.AuditLog;
import persistence.entity.Profile;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class PersistenceSupport {

    private PersistenceSupport() {
    }

    public static CrudController<ProfileDTO, Profile> profileController(EntityRepository<Profile> profileRepository) {
        return RouteSupport.crudController(
                new MappedReadRepository<>(profileRepository, profile -> DtoMappers.toProfileDto(profile)),
                profileRepository,
                Profile.class
        );
    }

    public static CrudController<AuditLogDTO, AuditLog> auditLogController(EntityRepository<AuditLog> auditLogRepository) {
        return RouteSupport.crudController(
                new ReadRepository<>() {
                    @Override
                    public List<AuditLogDTO> findAll() {
                        return auditLogRepository.findAll().stream()
                                .sorted(Comparator.comparing(
                                        auditLog -> auditLog.getChangedAt(),
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                ))
                                .map(auditLog -> DtoMappers.toAuditLogDto(auditLog))
                                .toList();
                    }

                    @Override
                    public AuditLogDTO findById(Long id) {
                        AuditLog auditLog = auditLogRepository.findById(id);
                        return auditLog == null ? null : DtoMappers.toAuditLogDto(auditLog);
                    }
                },
                auditLogRepository,
                AuditLog.class
        );
    }

    public static <D, E> CrudController<D, E> mappedCrudController(
            EntityRepository<E> repository,
            Function<E, D> mapper,
            Class<E> entityClass
    ) {
        return RouteSupport.crudController(
                new MappedReadRepository<>(repository, mapper),
                repository,
                entityClass
        );
    }

    public static BackendControllers controllers(BackendRepositories repositories) {
        AuthService authService = new AuthService(repositories.profileRepository());
        return new BackendControllers(
                authService,
                profileController(repositories.profileRepository()),
                auditLogController(repositories.auditLogRepository()),
                mappedCrudController(repositories.drugRepository(), drug -> DtoMappers.toDrugDto(drug), persistence.entity.Drug.class),
                mappedCrudController(repositories.questRepository(), quest -> DtoMappers.toQuestDto(quest), persistence.entity.Quest.class),
                mappedCrudController(repositories.characterRepository(), character -> DtoMappers.toGameCharacterDto(character), persistence.entity.GameCharacter.class),
                mappedCrudController(repositories.houseRepository(), house -> DtoMappers.toHouseDto(house), persistence.entity.House.class),
                mappedCrudController(repositories.garageRepository(), garage -> DtoMappers.toGarageDto(garage), persistence.entity.Garage.class),
                mappedCrudController(repositories.vehicleRepository(), vehicle -> DtoMappers.toVehicleDto(vehicle), persistence.entity.Vehicle.class),
                mappedCrudController(repositories.characterDrugRepository(), characterDrug -> DtoMappers.toCharacterDrugDto(characterDrug), persistence.entity.CharacterDrug.class),
                mappedCrudController(repositories.characterQuestRepository(), characterQuest -> DtoMappers.toCharacterQuestDto(characterQuest), persistence.entity.CharacterQuest.class),
                mappedCrudController(repositories.gangRepository(), gang -> DtoMappers.toGangDto(gang), persistence.entity.Gang.class),
                mappedCrudController(repositories.gangAffiliationRepository(), gangAffiliation -> DtoMappers.toGangAffiliationDto(gangAffiliation), persistence.entity.GangAffiliation.class)
        );
    }
}
