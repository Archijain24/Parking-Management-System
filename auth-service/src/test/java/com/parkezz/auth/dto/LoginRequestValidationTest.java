package com.parkezz.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestValidationTest {

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

    @Test
    void validRequest_hasNoViolations() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void blankEmail_isRejected() {
        LoginRequest request = new LoginRequest("", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email is required"));
    }

    @Test
    void invalidEmailFormat_isRejected() {
        LoginRequest request = new LoginRequest("hello", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email must be a valid email address"));
    }

    @Test
    void blankPassword_isRejected() {
        LoginRequest request = new LoginRequest("john@example.com", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Password is required"));
    }

    @Test
    void nullPassword_isRejected() {
        LoginRequest request = new LoginRequest("john@example.com", null);
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
