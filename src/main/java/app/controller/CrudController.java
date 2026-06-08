package app.controller;

import app.dao.ReadRepository;
import app.dao.WriteRepository;
import app.setup.EntityIds;
import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;

public class CrudController<R, W> {

    private final ReadRepository<R> readDao;
    private final WriteRepository<W> writeDao;
    private final Class<W> bodyClass;

    public CrudController(ReadRepository<R> readDao, WriteRepository<W> writeDao, Class<W> bodyClass) {
        this.readDao = readDao;
        this.writeDao = writeDao;
        this.bodyClass = bodyClass;
    }

    public void getAll(Context ctx) {
        ctx.json(readDao.findAll());
    }

    public void getById(Context ctx) {
        Long id = parseId(ctx);
        R result = readDao.findById(id);
        if (result == null) {
            throw new NotFoundResponse("Not found");
        }
        ctx.json(result);
    }

    public void create(Context ctx) {
        W entity = ctx.bodyAsClass(bodyClass);
        W saved = writeDao.save(entity);
        ctx.status(201).json(responseBody(saved));
    }

    public void update(Context ctx) {
        W entity = ctx.bodyAsClass(bodyClass);
        W updated = writeDao.update(entity);
        ctx.json(responseBody(updated));
    }

    public void delete(Context ctx) {
        writeDao.deleteById(parseId(ctx));
        ctx.status(204);
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid id");
        }
    }

    private Object responseBody(W entity) {
        Long id = EntityIds.get(entity);
        if (id == null) {
            return entity;
        }
        R result = readDao.findById(id);
        return result == null ? entity : result;
    }
}
