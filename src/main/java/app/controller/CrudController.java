package app.controller;

import app.dao.AbstractJpaDao;
import app.dao.JpaReadDao;
import io.javalin.http.Context;

public class CrudController<R, W> {

    private final JpaReadDao<R> readDao;
    private final AbstractJpaDao<W> writeDao;
    private final Class<W> bodyClass;

    public CrudController(JpaReadDao<R> readDao, AbstractJpaDao<W> writeDao, Class<W> bodyClass) {
        this.readDao = readDao;
        this.writeDao = writeDao;
        this.bodyClass = bodyClass;
    }

    public void getAll(Context ctx) {
        ctx.json(readDao.findAll());
    }

    public void getById(Context ctx) {
        Long id = parseId(ctx);
        if (id == null) {
            return;
        }

        R result = readDao.findById(id);
        if (result == null) {
            ctx.status(404).json("Not found");
            return;
        }
        ctx.json(result);
    }

    public void create(Context ctx) {
        W entity = ctx.bodyAsClass(bodyClass);
        W saved = writeDao.save(entity);
        ctx.status(201).json(saved);
    }

    public void update(Context ctx) {
        W entity = ctx.bodyAsClass(bodyClass);
        W updated = writeDao.update(entity);
        ctx.json(updated);
    }

    public void delete(Context ctx) {
        Long id = parseId(ctx);
        if (id == null) {
            return;
        }

        writeDao.deleteById(id);
        ctx.status(204);
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json("Invalid id");
            return null;
        }
    }
}
