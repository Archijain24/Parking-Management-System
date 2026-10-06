package com.parkezz.parking.dto;

import com.parkezz.parking.entity.ParkingStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingRequestValidationTest {

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

    private ParkingRequest validRequest() {
        return new ParkingRequest("Downtown", "123 Main St", BigDecimal.valueOf(50), "CAR", ParkingStatus.AVAILABLE);
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void blankLocation_isRejected() {
        ParkingRequest request = validRequest();
        request.setLocation("");
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Location is required"));
    }

    @Test
    void blankAddress_isRejected() {
        ParkingRequest request = validRequest();
        request.setAddress("");
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Address is required"));
    }

    @Test
    void nullPrice_isRejected() {
        ParkingRequest request = validRequest();
        request.setPricePerHour(null);
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Price per hour is required"));
    }

    @Test
    void zeroPrice_isRejected() {
        ParkingRequest request = validRequest();
        request.setPricePerHour(BigDecimal.ZERO);
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("greater than zero"));
    }

    @Test
    void negativePrice_isRejected() {
        ParkingRequest request = validRequest();
        request.setPricePerHour(BigDecimal.valueOf(-10));
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("greater than zero"));
    }

    @Test
    void blankVehicleType_isRejected() {
        ParkingRequest request = validRequest();
        request.setVehicleType("");
        Set<ConstraintViolation<ParkingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Vehicle type is required"));
    }
}
