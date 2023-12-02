package com.bds.controllers;

import com.bds.dto.UsersRegistrationRequest;
import com.bds.models.BloodType;
import com.bds.models.Role;
import com.bds.models.Users;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class UsersControllerTest {

    @Autowired
    WebTestClient webTestClient;

    private static final String usersURI = "api/v1/admin";


    @Test
    void canRegisterNewUser() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all users
        List<Users> allUsers = webTestClient.get()
                .uri(usersURI + "/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // make sure that user is present
        Users expectedUser = new Users(
                firstName,
                lastName,
                email,
                Role.DONOR,
                BloodType.APos
        );

        assertThat(allUsers)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("Id")
                .contains(expectedUser);
    }

    @Test
    void badFirstNameFormatTooShort() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = "m";
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badFirstNameFormatTooLong() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = "m12345689m123456789m123456789m123456789";
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badLastNameFormatTooShort() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = "b";
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badLastNameFormatTooLong() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = "b123456789b123456789b123456789b123456798";
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badEmailFormat() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.firstName();
        String email = "@";
        String role = "DONOR";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badRoleFormat() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "D";
        String bloodType = "APos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badBloodTypeFormat() {
        // create registration request
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";
        String role = "DONOR";
        String bloodType = "Aos";

        UsersRegistrationRequest request = new UsersRegistrationRequest(
                firstName,
                lastName,
                email,
                role,
                bloodType
        );

        // send a post request
        webTestClient.post()
                .uri(usersURI + "/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }
}
