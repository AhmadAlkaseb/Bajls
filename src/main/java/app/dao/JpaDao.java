package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaQuery;
import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.Garage;
import persistence.entity.Profile;
import persistence.entity.Quest;
import persistence.entity.Vehicle;

import java.util.List;

public class JpaDao<T> implements EntityRepository<T> {
    private final EntityManagerFactory entityManagerFactory;
    private final Class<T> entityClass;

    public JpaDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            attachReferences(entity, em);
            em.persist(entity);
            em.flush();
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public T update(T entity) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            attachReferences(entity, em);
            T merged = em.merge(entity);
            em.flush();
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public void deleteById(Long id) {
        EntityTransaction tx = null;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            rollbackIfActive(tx);
            throw e;
        }
    }

    public T findById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            CriteriaQuery<T> criteria = em.getCriteriaBuilder().createQuery(entityClass);
            criteria.from(entityClass);
            return em.createQuery(criteria).getResultList();
        }
    }

    protected EntityManagerFactory entityManagerFactory() {
        return entityManagerFactory;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    private void rollbackIfActive(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }

    private void attachReferences(T entity, EntityManager em) {
        if (entity instanceof GameCharacter character && hasId(character.getProfile())) {
            character.setProfile(em.getReference(Profile.class, character.getProfile().getId()));
        }
        if (entity instanceof Vehicle vehicle && hasId(vehicle.getGarage())) {
            vehicle.setGarage(em.getReference(Garage.class, vehicle.getGarage().getId()));
        }
        if (entity instanceof GangAffiliation affiliation) {
            if (hasId(affiliation.getCharacter())) {
                affiliation.setCharacter(em.getReference(GameCharacter.class, affiliation.getCharacter().getId()));
            }
            if (hasId(affiliation.getGang())) {
                affiliation.setGang(em.getReference(Gang.class, affiliation.getGang().getId()));
            }
        }
        if (entity instanceof CharacterDrug characterDrug) {
            if (hasId(characterDrug.getCharacter())) {
                characterDrug.setCharacter(em.getReference(GameCharacter.class, characterDrug.getCharacter().getId()));
            }
            if (hasId(characterDrug.getDrug())) {
                characterDrug.setDrug(em.getReference(Drug.class, characterDrug.getDrug().getId()));
            }
        }
        if (entity instanceof CharacterQuest characterQuest) {
            if (hasId(characterQuest.getCharacter())) {
                characterQuest.setCharacter(em.getReference(GameCharacter.class, characterQuest.getCharacter().getId()));
            }
            if (hasId(characterQuest.getQuest())) {
                characterQuest.setQuest(em.getReference(Quest.class, characterQuest.getQuest().getId()));
            }
        }
    }

    private boolean hasId(Profile profile) {
        return profile != null && profile.getId() != null;
    }

    private boolean hasId(Garage garage) {
        return garage != null && garage.getId() != null;
    }

    private boolean hasId(GameCharacter character) {
        return character != null && character.getId() != null;
    }

    private boolean hasId(Gang gang) {
        return gang != null && gang.getId() != null;
    }

    private boolean hasId(Drug drug) {
        return drug != null && drug.getId() != null;
    }

    private boolean hasId(Quest quest) {
        return quest != null && quest.getId() != null;
    }
}
