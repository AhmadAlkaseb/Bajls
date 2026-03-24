package app.setup;

public final class ReadQuery<R> {
    private final String listJpql;
    private final String byIdJpql;
    private final Class<R> dtoClass;

    public ReadQuery(String listJpql, String byIdJpql, Class<R> dtoClass) {
        this.listJpql = listJpql;
        this.byIdJpql = byIdJpql;
        this.dtoClass = dtoClass;
    }

    public String getListJpql() {
        return listJpql;
    }

    public String getByIdJpql() {
        return byIdJpql;
    }

    public Class<R> getDtoClass() {
        return dtoClass;
    }
}
