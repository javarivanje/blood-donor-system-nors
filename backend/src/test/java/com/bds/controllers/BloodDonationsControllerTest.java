package com.bds.controllers;

import com.bds.dto.*;
import com.bds.models.BloodDonations;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class BloodDonationsControllerTest {

    @Autowired
    WebTestClient webTestClient;

    private static final String bloodDonationURI = "api/v1";

    @Test
    void canAddBloodDonation() {
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
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri(bloodDonationURI + "/admin")
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

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // make count available blood units request
        List<BloodUnitsImpl> availableBloodUnits = webTestClient.get()
                .uri(bloodDonationURI + "/admin/available_blood_units")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodUnitsImpl>() {
                })
                .returnResult()
                .getResponseBody();

        // make blood donation request
        BloodDonationRequest bloodDonationRequest =
                new BloodDonationRequest(
                        5,
                        LocalDate.now(),
                        donorUser,
                        adminUser
                );

        // send a post request and enter blood donation
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/enter_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationRequest), BloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // make sure that blood donation is present
        Long donorId = allDonors.stream()
                .filter(donor -> donor.getEmail().equals(donorUser.getEmail()))
                .map(d -> d.getId())
                .findFirst()
                .orElseThrow();

        List<BloodDonations> allBloodDonations = webTestClient.get()
                .uri(bloodDonationURI + "/donor/my_blood_donations/" + donorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodDonations>() {
                })
                .returnResult()
                .getResponseBody();

        BloodDonations expectedBloodDonation = new BloodDonations(
                5,
                LocalDate.now(),
                donorUser,
                adminUser
        );

        assertThat(allBloodDonations)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("Id")
                .contains(expectedBloodDonation);

        // make count available blood units request
        List<BloodUnitsImpl> updatedAvailableBloodUnits = webTestClient.get()
                .uri(bloodDonationURI + "/admin/available_blood_units")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodUnitsImpl>() {
                })
                .returnResult()
                .getResponseBody();

        // compare values you get with the values you had plus values added
        assertThat(
                updatedAvailableBloodUnits.stream()
                        .filter(bloodUnits -> bloodUnits.getBloodType().equals(donorUser.getBloodType().toString()))
                        .map(BloodUnitsImpl::getTotalUnits)
                        .findFirst())
                .isEqualTo(
                        availableBloodUnits.stream()
                                .filter(bloodType -> bloodType.getBloodType().equals(donorUser.getBloodType().toString()))
                                .map(bloodUnits -> bloodUnits.getTotalUnits() + expectedBloodDonation.getUnits())
                                .findFirst()
                );
    }

    @Test
    void canInitiateAndConfirmBloodDonation() {
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
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri(bloodDonationURI + "/admin")
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

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // make count available blood units request
        List<BloodUnitsImpl> availableBloodUnits = webTestClient.get()
                .uri(bloodDonationURI + "/admin/available_blood_units")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodUnitsImpl>() {
                })
                .returnResult()
                .getResponseBody();

        // initiate blood donation
        InitiateBloodDonationRequest initiateBloodDonationRequest = new InitiateBloodDonationRequest(
                donorUser,
                11,
                LocalDate.now()
        );

        webTestClient.post()
                .uri(bloodDonationURI + "/donor/initiate_blood_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(initiateBloodDonationRequest), InitiateBloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        // make sure that blood donation is present
        Long donorId = allDonors.stream()
                .filter(donor -> donor.getEmail().equals(donorUser.getEmail()))
                .map(d -> d.getId())
                .findFirst()
                .orElseThrow();

        List<BloodDonations> allBloodDonations = webTestClient.get()
                .uri(bloodDonationURI + "/donor/my_blood_donations/" + donorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodDonations>() {
                })
                .returnResult()
                .getResponseBody();

        BloodDonations expectedBloodDonation = new BloodDonations(
                11,
                LocalDate.now(),
                donorUser
        );

        assertThat(allBloodDonations)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("Id")
                .contains(expectedBloodDonation);

        // confirm blood donation
        Long donationId = allBloodDonations.stream()
                .filter(bd -> bd.getDonationDate().equals(LocalDate.now()) && bd.getDonor().equals(donorUser))
                .map(BloodDonations::getId)
                .findFirst()
                .orElseThrow();

        ConfirmDonationRequest confirmDonationRequest = new ConfirmDonationRequest(
                adminUser.getId(),
                11
        );

        webTestClient.patch()
                .uri(bloodDonationURI + "/admin/confirm_blood_donation/" + donationId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(confirmDonationRequest), ConfirmDonationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();


        // make count available blood units request
        List<BloodUnitsImpl> updatedAvailableBloodUnits = webTestClient.get()
                .uri(bloodDonationURI + "/admin/available_blood_units")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<BloodUnitsImpl>() {
                })
                .returnResult()
                .getResponseBody();

        // compare values you get with the values you had plus values added
        assertThat(
                updatedAvailableBloodUnits.stream()
                        .filter(bloodUnits -> bloodUnits.getBloodType().equals(donorUser.getBloodType().toString()))
                        .map(BloodUnitsImpl::getTotalUnits)
                        .findFirst())
                .isEqualTo(
                        availableBloodUnits.stream()
                                .filter(bloodType -> bloodType.getBloodType().equals(donorUser.getBloodType().toString()))
                                .map(bloodUnits -> bloodUnits.getTotalUnits() + expectedBloodDonation.getUnits())
                                .findFirst()
                );
    }

    @Test
    void badUnitsFormatInBloodDonationRequestMustBeGreaterThanZero() {
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
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri(bloodDonationURI + "/admin")
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

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation request
        BloodDonationRequest bloodDonationRequest =
                new BloodDonationRequest(
                        0,
                        LocalDate.now(),
                        donorUser,
                        adminUser
                );

        // send a post request and enter blood donation
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/enter_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationRequest), BloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badDonationDateFormatInBloodDonationRequestShouldBeFutureOrPresent() {
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
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(adminRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all admin users
        List<Users> allAdmins = webTestClient.get()
                .uri(bloodDonationURI + "/admin")
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

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // make blood donation request
        BloodDonationRequest bloodDonationRequest =
                new BloodDonationRequest(
                        0,
                        LocalDate.now().minusYears(10L),
                        donorUser,
                        adminUser
                );

        // send a post request and enter blood donation
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/enter_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bloodDonationRequest), BloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badUnitsFormatInInitiateBloodDonationRequestShouldBeGreaterThanZero() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // initiate blood donation
        InitiateBloodDonationRequest initiateBloodDonationRequest = new InitiateBloodDonationRequest(
                donorUser,
                0,
                LocalDate.now()
        );

        webTestClient.post()
                .uri(bloodDonationURI + "/donor/initiate_blood_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(initiateBloodDonationRequest), InitiateBloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }

    @Test
    void badDonationDateFormatInInitiateBloodDonationRequestShouldBeFutureOrPresent() {
        // create faker user
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String firstName = fakerName.firstName();
        String lastName = fakerName.lastName();
        String email = fakerName.lastName() + UUID.randomUUID() + "@integrationTest.com";

        // create donor registration request
        UsersRegistrationRequest donorRequest = new UsersRegistrationRequest(
                firstName,
                lastName,
                email + "---DONOR",
                "DONOR",
                "OPos"
        );

        // register donor user
        webTestClient.post()
                .uri(bloodDonationURI + "/admin/register_user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(donorRequest), UsersRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isCreated();

        // get all donor users
        List<Users> allDonors = webTestClient.get()
                .uri(bloodDonationURI + "/admin/donor")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Users>() {
                })
                .returnResult()
                .getResponseBody();

        // get donor user from users
        Users donorUser = allDonors.stream()
                .filter(user -> user.getEmail().equals(donorRequest.email()))
                .findFirst()
                .orElseThrow();

        // initiate blood donation
        InitiateBloodDonationRequest initiateBloodDonationRequest = new InitiateBloodDonationRequest(
                donorUser,
                0,
                LocalDate.now().minusYears(15L)
        );

        webTestClient.post()
                .uri(bloodDonationURI + "/donor/initiate_blood_donation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(initiateBloodDonationRequest), InitiateBloodDonationRequest.class)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(406));
    }
}
