package app.dao;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Role;
public class RoleDao extends AbstractJpaDao<Role> {
    public RoleDao(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, Role.class);
    }
}
