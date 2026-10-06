package com.parkezz.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestValidationTest {

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
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingRequest request = new BookingRequest(1L, start, start.plusHours(2));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void nullParkingId_isRejected() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingRequest request = new BookingRequest(null, start, start.plusHours(1));
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Parking ID is required"));
    }

    @Test
    void startTimeInPast_isRejected() {
        BookingRequest request = new BookingRequest(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Start time must be in the future"));
    }

    @Test
    void endTimeInPast_isRejected() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingRequest request = new BookingRequest(1L, start, LocalDateTime.now().minusHours(1));
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("End time must be in the future"));
    }

    @Test
    void nullStartTime_isRejected() {
        BookingRequest request = new BookingRequest(1L, null, LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Start time is required"));
    }
}
