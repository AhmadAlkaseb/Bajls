package integration;

import app.setup.AppPersistence;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class Neo4jIntegrationTest {

    static final String NEO4J_URI = "bolt://localhost:7687";

    static Driver driver;
    static AppPersistence persistence;
    static int port;

    @BeforeAll
    static void startAll() {
        // Use local Neo4j instance via system properties.
        // Note: Neo4j:5 has authentication enabled by default (neo4j:password)
        System.setProperty("NEO4J_URI", NEO4J_URI);
        System.setProperty("NEO4J_USER", "neo4j");
        System.setProperty("NEO4J_PASSWORD", "password");

        driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic("neo4j", "password"));
        persistence = PersistenceBootstrap.createPersistence(DatabaseType.NEO4J, false);
        port = TestServer.start(persistence);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @AfterAll
    static void stopAll() throws Exception {
        if (persistence != null) persistence.close();
        if (driver != null) driver.close();

        System.clearProperty("NEO4J_URI");
        System.clearProperty("NEO4J_USER");
        System.clearProperty("NEO4J_PASSWORD");
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }


    @Test
    @DisplayName("Register new user")
    void register() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Test", "lastName": "User",
                          "email": "alice@test.com", "username": "alice",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("username", equalTo("alice"))
                .body("role", equalTo("USER"))
                .body("profileId", notNullValue());
    }

    @Test
    @DisplayName("Login with valid credentials")
    void login() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Test", "lastName": "User",
                          "email": "bob@test.com", "username": "bob",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "bob",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo("bob"))
                .body("role", equalTo("USER"))
                .body("profileId", notNullValue());
    }
}