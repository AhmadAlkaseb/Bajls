package app.controller;

import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

public class ReadController<T> {

    private final EntityManagerFactory emf;
    private final String listJpql;
    private final String byIdJpql;
    private final Class<T> dtoClass;

    public ReadController(EntityManagerFactory emf, String listJpql, String byIdJpql, Class<T> dtoClass) {
        this.emf = emf;
        this.listJpql = listJpql;
        this.byIdJpql = byIdJpql;
        this.dtoClass = dtoClass;
    }

    public void getAll(Context ctx) {
        EntityManager em = emf.createEntityManager();
        try {
            ctx.json(em.createQuery(listJpql, dtoClass).getResultList());
        } finally {
            em.close();
        }
    }

    public void getById(Context ctx) {
        Integer id = parseId(ctx);
        if (id == null) {
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            Optional<T> result = em.createQuery(byIdJpql, dtoClass)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();

            if (result.isPresent()) {
                ctx.json(result.get());
            } else {
                ctx.status(404).json("Not found");
            }
        } finally {
            em.close();
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
