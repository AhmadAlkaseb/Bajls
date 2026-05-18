package integration;

import app.setup.AppPersistence;
import app.setup.DatabaseType;
import app.setup.PersistenceBootstrap;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class PostgresIntegrationTest {

    static AppPersistence persistence;
    static int port;

    @BeforeAll
    static void startAll() {
        persistence = PersistenceBootstrap.createPersistence(DatabaseType.POSTGRES, true);
        port = TestServer.start(persistence);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @AfterAll
    static void stopAll() throws Exception {
        if (persistence != null) persistence.close();
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
                          "email": "pg-register@test.com", "username": "pg-register",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("username", equalTo("pg-register"))
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
                          "email": "pg-login@test.com", "username": "pg-login",
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
                          "username": "pg-login",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo("pg-login"))
                .body("role", equalTo("USER"))
                .body("profileId", notNullValue());
    }
}