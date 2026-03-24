package app.dao;

import java.util.List;
import java.util.function.Function;

public class MappedReadRepository<R, E> implements ReadRepository<R> {
    private final ReadRepository<E> sourceRepository;
    private final Function<E, R> mapper;

    public MappedReadRepository(ReadRepository<E> sourceRepository, Function<E, R> mapper) {
        this.sourceRepository = sourceRepository;
        this.mapper = mapper;
    }

    @Override
    public List<R> findAll() {
        return sourceRepository.findAll().stream()
                .map(mapper)
                .toList();
    }

    @Override
    public R findById(Long id) {
        E entity = sourceRepository.findById(id);
        return entity == null ? null : mapper.apply(entity);
    }
}
