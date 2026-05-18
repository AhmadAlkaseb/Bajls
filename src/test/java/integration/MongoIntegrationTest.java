package integration;

import app.setup.AppPersistence;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class MongoIntegrationTest {

    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");
    static final String DB_NAME = "bajls_test";

    static MongoClient client;
    static AppPersistence persistence;
    static int port;

    @BeforeAll
    static void startAll() {
        MONGO.start();

        System.setProperty("MONGO_URL", MONGO.getReplicaSetUrl());
        System.setProperty("MONGO_DB_NAME", DB_NAME);

        client = MongoClients.create(MONGO.getReplicaSetUrl());
        persistence = PersistenceBootstrap.createPersistence(DatabaseType.MONGODB, false);
        port = TestServer.start(persistence);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @AfterAll
    static void stopAll() throws Exception {
        if (persistence != null) persistence.close();
        if (client != null) client.close();
        MONGO.stop();

        System.clearProperty("MONGO_URL");
        System.clearProperty("MONGO_DB_NAME");
    }

    @BeforeEach
    void cleanDatabase() {
        client.getDatabase(DB_NAME).drop();
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