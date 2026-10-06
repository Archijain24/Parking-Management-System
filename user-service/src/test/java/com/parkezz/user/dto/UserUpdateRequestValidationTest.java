package com.parkezz.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdateRequestValidationTest {

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

    private UserUpdateRequest validRequest() {
        return new UserUpdateRequest("John Doe", "john@example.com", "9876543210");
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        UserUpdateRequest request = validRequest();
        request.setName(" ");

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Name is required"));
    }

    @Test
    void invalidEmail_isRejected() {
        UserUpdateRequest request = validRequest();
        request.setEmail("not-an-email");

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email must be a valid email address"));
    }

    @Test
    void invalidPhone_isRejected() {
        UserUpdateRequest request = validRequest();
        request.setPhone("abc1234567");

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("10 digits"));
    }
}
