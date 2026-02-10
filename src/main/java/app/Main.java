package app;

import persistence.HibernateConfig;

public class Main {
    public static void main(String[] args) {
        HibernateConfig.getEntityManagerFactoryConfig(false);
    }
}
