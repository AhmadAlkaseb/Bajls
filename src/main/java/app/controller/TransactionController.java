package app.controller;

import app.dao.JpaReadDao;
import app.dto.BuyDrugRequest;
import app.dto.TransactionDTO;
import app.dto.TransferRequest;
import app.setup.DtoMappers;
import app.setup.RouteQueries;
import app.transaction.TransactionService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Transaction;

import java.util.List;

public class TransactionController {

    private final JpaReadDao<TransactionDTO> readDao;
    private final TransactionService transactionService;

    public TransactionController(EntityManagerFactory emf) {
        this.readDao = new JpaReadDao<>(
                emf,
                RouteQueries.TRANSACTION_QUERY.getListJpql(),
                RouteQueries.TRANSACTION_QUERY.getByIdJpql(),
                TransactionDTO.class
        );
        this.transactionService = new TransactionService(emf);
    }

    public void getAll(io.javalin.http.Context ctx) {
        ctx.json(readDao.findAll());
    }

    public void getById(io.javalin.http.Context ctx) {
        Long id = parseId(ctx);
        TransactionDTO result = readDao.findById(id);
        if (result == null) throw new NotFoundResponse("Transaction not found");
        ctx.json(result);
    }

    public void buyDrug(io.javalin.http.Context ctx) {
        BuyDrugRequest req = ctx.bodyAsClass(BuyDrugRequest.class);
        Transaction transaction = transactionService.buyDrug(
                req.getCharacterId(),
                req.getDrugId(),
                req.getQuantity(),
                req.getPricePerUnit()
        );
        ctx.status(201).json(DtoMappers.toTransactionDto(transaction));
    }

    public void transfer(io.javalin.http.Context ctx) {
        TransferRequest req = ctx.bodyAsClass(TransferRequest.class);
        List<Transaction> transactions = transactionService.transfer(
                req.getFromCharacterId(),
                req.getToCharacterId(),
                req.getAmount()
        );
        ctx.status(201).json(transactions.stream().map(DtoMappers::toTransactionDto).toList());
    }

    private Long parseId(io.javalin.http.Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid id");
        }
    }
}
