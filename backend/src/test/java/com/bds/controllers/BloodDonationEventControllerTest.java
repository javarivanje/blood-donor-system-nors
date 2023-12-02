package com.bds.controllers;

import com.bds.dto.BloodDonationEventRequest;
import com.bds.dto.UsersRegistrationRequest;
import com.bds.models.BloodDonationEvent;
import com.bds.models.BloodType;
import com.bds.models.Users;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class BloodDonationEventControllerTest {

    @Autowired
    WebTestClient webTestClient;

    private static final String donationEventURI = "api/v1/admin";

    @Test
    void canAddBloodDonationEvent() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = faker.company().name();
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now(),
                "ONeg",
                5,
                adminUser
        );

        // send a post request for registering new event
        BloodDonationEvent savedBloodDonationEvent = webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<BloodDonationEvent>() {
                })
                .returnResult()
                .getResponseBody();

        // make sure that event is saved

        BloodDonationEvent expectedBloodDonationEvent = new BloodDonationEvent(
                eventName,
                LocalDate.now(),
                BloodType.valueOf("ONeg"),
                5,
                adminUser
        );

        RecursiveComparisonConfiguration ignoreIdConfig = new RecursiveComparisonConfiguration();
        ignoreIdConfig.ignoreFields("Id");

        assertThat(savedBloodDonationEvent)
                .usingRecursiveComparison(ignoreIdConfig)
                .isEqualTo(expectedBloodDonationEvent);
    }

    @Test
    void badEventNameFormatEmptyEventName() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = "";
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now(),
                "ONeg",
                5,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badEventNameFormatTooShort() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = "a";
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now(),
                "ONeg",
                5,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badEventNameFormatTooLong() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = "b123456789b123456789b123456798b12345";
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now(),
                "ONeg",
                5,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badEventDateFormatDateIsNotFutureOrPresent() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = faker.company().name().toString();
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now().minusYears(1L),
                "ONeg",
                5,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badBloodTypeFormat() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = faker.company().name().toString();
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now().minusYears(1L),
                "O",
                5,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badUnitsFormatLesThanZero() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = faker.company().name().toString();
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now().minusYears(1L),
                "ONeg",
                -1,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badUnitsFormatGreaterThanFive() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create admin registration request
        UsersRegistrationRequest adminRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---ADMIN",
                "ADMIN",
                "ABPos"
        );

        // register admin user
        webTestClient.post()
                .uri("/api/v1/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri("/api/v1/admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get admin user from users
        Users adminUser = allAdmins.stream()
                .filter(user -> user.getEmail().equals(adminRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation event request
        String eventName = faker.company().name().toString();
        BloodDonationEventRequest bloodDonationEventRequest = new BloodDonationEventRequest(
                eventName,
                LocalDate.now().minusYears(1L),
                "ONeg",
                10,
                adminUser
        );

        // send a post request for registering new event
        webTestClient.post()
                .uri(donationEventURI + "/donation_event")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationEventRequest), BloodDonationEventRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }
}
