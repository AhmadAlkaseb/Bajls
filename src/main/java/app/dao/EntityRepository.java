package app.dao;

public interface EntityRepository<T> extends ReadRepository<T>, WriteRepository<T> {
}
