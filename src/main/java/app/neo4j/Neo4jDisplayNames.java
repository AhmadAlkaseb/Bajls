package app.neo4j;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public final class Neo4jDisplayNames {
    private Neo4jDisplayNames() {
    }

    public static void apply(Driver driver) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n:Drug) SET n.name = coalesce(n.name, 'Drug ' + toString(n.id))");
                tx.run("MATCH (n:GameCharacter) SET n.name = coalesce(n.name, 'Character ' + toString(n.id))");
                tx.run("MATCH (n:Gang) SET n.name = coalesce(n.name, 'Gang ' + toString(n.id))");
                tx.run("MATCH (n:Garage) SET n.name = coalesce(n.name, 'Garage ' + toString(n.id))");
                tx.run("MATCH (n:House) SET n.name = coalesce(n.name, 'House ' + toString(n.id))");
                tx.run("MATCH (n:Profile) SET n.name = coalesce(n.name, n.username, 'Profile ' + toString(n.id))");
                tx.run("MATCH (n:Quest) SET n.name = coalesce(n.name, n.title, 'Quest ' + toString(n.id))");
                tx.run("MATCH (n:Vehicle) SET n.name = coalesce(n.name, n.model, 'Vehicle ' + toString(n.id))");
                tx.run("""
                        MATCH (n)
                        WHERE n:Drug OR n:GameCharacter OR n:Gang OR n:Garage
                           OR n:House OR n:Profile OR n:Quest OR n:Vehicle
                        SET n.displayName = n.name
                        """);
                return null;
            });
        }
    }
}
