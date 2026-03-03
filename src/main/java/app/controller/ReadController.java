package app.controller;

import app.dao.ReadDao;
import io.javalin.http.Context;

import java.util.Optional;

public class ReadController<T> {

    private final ReadDao<T> readDao;

    public ReadController(ReadDao<T> readDao) {
        this.readDao = readDao;
    }

    public void getAll(Context ctx) {
        ctx.json(readDao.findAll());
    }

    public void getById(Context ctx) {
        Integer id = parseId(ctx);
        if (id == null) {
            return;
        }

        Optional<T> result = readDao.findById(id);
        if (result.isPresent()) {
            ctx.json(result.get());
        } else {
            ctx.status(404).json("Not found");
        }
    }

    private Integer parseId(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json("Invalid id");
            return null;
        }
    }
}
