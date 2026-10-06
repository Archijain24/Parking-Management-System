package com.parkezz.user.dto;

import com.parkezz.user.entity.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private RegisterRequest validRequest() {
        return new RegisterRequest("John Doe", "john@example.com", "password123", "9876543210", Role.CUSTOMER);
    }

    @Test
    void validRequest_hasNoViolations() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        RegisterRequest request = validRequest();
        request.setName("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Name is required"));
    }

    @Test
    void nullName_isRejected() {
        RegisterRequest request = validRequest();
        request.setName(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void tooShortName_isRejected() {
        RegisterRequest request = validRequest();
        request.setName("A");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("between 2 and 100"));
    }

    @Test
    void invalidEmailFormat_isRejectedWithFriendlyMessage() {
        RegisterRequest request = validRequest();
        request.setEmail("hello");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email must be a valid email address"));
    }

    @Test
    void blankEmail_isRejected() {
        RegisterRequest request = validRequest();
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email is required"));
    }

    @Test
    void shortPassword_isRejected() {
        RegisterRequest request = validRequest();
        request.setPassword("12345");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("at least 6 characters"));
    }

    @Test
    void boundaryPassword_sixCharacters_isAccepted() {
        RegisterRequest request = validRequest();
        request.setPassword("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidPhone_wrongLength_isRejected() {
        RegisterRequest request = validRequest();
        request.setPhone("12345");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("10 digits"));
    }

    @Test
    void invalidPhone_containsLetters_isRejected() {
        RegisterRequest request = validRequest();
        request.setPhone("98765abcde");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("10 digits"));
    }

    @Test
    void nullRole_isRejected() {
        RegisterRequest request = validRequest();
        request.setRole(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Role is required"));
    }
}
