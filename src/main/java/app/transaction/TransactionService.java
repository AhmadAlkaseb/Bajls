package app.transaction;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.*;
import persistence.entity.CharacterDrug;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;
import persistence.entity.Transaction;
import persistence.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles balance-changing operations with optimistic locking.
 *
 * Concurrency model: GameCharacter has a @Version field. When two requests
 * read the same character simultaneously and both try to commit a balance
 * change, the second commit will find the version in the DB has already
 * been incremented by the first commit and throw OptimisticLockException.
 * This prevents double-deduction without holding any DB-level locks.
 *
 * On conflict, HTTP 409 is returned so the client can retry.
 */
public class TransactionService {

    private final EntityManagerFactory emf;

    public TransactionService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Deducts totalCost (pricePerUnit * quantity) from the character's balance
     * and adds the drugs to the character's inventory.
     */
    public Transaction buyDrug(Long characterId, Long drugId, int quantity, BigDecimal pricePerUnit) {
        if (quantity <= 0) {
            throw new BadRequestResponse("Quantity must be positive");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestResponse("Price per unit must be zero or positive");
        }

        EntityTransaction tx = null;
        try (EntityManager em = emf.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();

            // LockModeType.OPTIMISTIC: Hibernate validates the @Version at commit time.
            // If another request committed a balance change in the meantime, this commit
            // will fail with OptimisticLockException instead of silently double-deducting.
            GameCharacter character = em.find(GameCharacter.class, characterId, LockModeType.OPTIMISTIC);
            if (character == null) {
                throw new NotFoundResponse("Character not found: " + characterId);
            }

            Drug drug = em.find(Drug.class, drugId);
            if (drug == null) {
                throw new NotFoundResponse("Drug not found: " + drugId);
            }

            BigDecimal totalCost = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
            if (character.getBalance().compareTo(totalCost) < 0) {
                throw new BadRequestResponse(
                        "Insufficient balance: needs " + totalCost + " but has " + character.getBalance()
                );
            }

            character.setBalance(character.getBalance().subtract(totalCost));

            // Upsert the character's drug inventory
            CharacterDrug existing = em.createQuery(
                            "SELECT cd FROM CharacterDrug cd WHERE cd.character.id = :charId AND cd.drug.id = :drugId",
                            CharacterDrug.class
                    )
                    .setParameter("charId", characterId)
                    .setParameter("drugId", drugId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + quantity);
            } else {
                em.persist(CharacterDrug.builder()
                        .character(character)
                        .drug(drug)
                        .quantity(quantity)
                        .build());
            }

            Transaction transaction = Transaction.builder()
                    .character(character)
                    .type(TransactionType.DRUG_PURCHASE)
                    .amount(totalCost.negate())
                    .drug(drug)
                    .quantity(quantity)
                    .description("Purchased " + quantity + "x " + drug.getName())
                    .createdAt(LocalDateTime.now())
                    .build();

            em.persist(transaction);
            tx.commit();
            return transaction;

        } catch (OptimisticLockException | RollbackException e) {
            rollbackIfActive(tx);
            if (isOptimisticLockFailure(e)) {
                throw new ConflictResponse(
                        "Balance was modified by a concurrent request. Please retry."
                );
            }
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    /**
     * Transfers amount from one character to another atomically.
     * Both balance changes and both Transaction records are committed in one DB transaction.
     * Returns the two Transaction records: [debit from sender, credit to receiver].
     */
    public List<Transaction> transfer(Long fromCharacterId, Long toCharacterId, BigDecimal amount) {
        if (fromCharacterId.equals(toCharacterId)) {
            throw new BadRequestResponse("Cannot transfer to the same character");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestResponse("Transfer amount must be positive");
        }

        EntityTransaction tx = null;
        try (EntityManager em = emf.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();

            // Lock both characters with optimistic locking.
            // To avoid any potential deadlock with pessimistic locking in future,
            // always load in consistent id-order (lowest id first).
            Long firstId = Math.min(fromCharacterId, toCharacterId);
            Long secondId = Math.max(fromCharacterId, toCharacterId);

            GameCharacter first = em.find(GameCharacter.class, firstId, LockModeType.OPTIMISTIC);
            GameCharacter second = em.find(GameCharacter.class, secondId, LockModeType.OPTIMISTIC);

            if (first == null) throw new NotFoundResponse("Character not found: " + firstId);
            if (second == null) throw new NotFoundResponse("Character not found: " + secondId);

            GameCharacter from = fromCharacterId.equals(firstId) ? first : second;
            GameCharacter to = toCharacterId.equals(firstId) ? first : second;

            if (from.getBalance().compareTo(amount) < 0) {
                throw new BadRequestResponse(
                        "Insufficient balance: needs " + amount + " but has " + from.getBalance()
                );
            }

            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));

            LocalDateTime now = LocalDateTime.now();

            Transaction debit = Transaction.builder()
                    .character(from)
                    .type(TransactionType.TRANSFER)
                    .amount(amount.negate())
                    .targetCharacter(to)
                    .description("Transfer to " + to.getName())
                    .createdAt(now)
                    .build();

            Transaction credit = Transaction.builder()
                    .character(to)
                    .type(TransactionType.TRANSFER)
                    .amount(amount)
                    .targetCharacter(from)
                    .description("Transfer from " + from.getName())
                    .createdAt(now)
                    .build();

            em.persist(debit);
            em.persist(credit);
            tx.commit();
            return List.of(debit, credit);

        } catch (OptimisticLockException | RollbackException e) {
            rollbackIfActive(tx);
            if (isOptimisticLockFailure(e)) {
                throw new ConflictResponse(
                        "Balance was modified by a concurrent request. Please retry."
                );
            }
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    private boolean isOptimisticLockFailure(Exception e) {
        return e instanceof OptimisticLockException
                || (e instanceof RollbackException && e.getCause() instanceof OptimisticLockException);
    }

    private void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
