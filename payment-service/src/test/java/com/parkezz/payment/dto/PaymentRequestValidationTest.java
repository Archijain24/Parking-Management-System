package com.parkezz.payment.dto;

import com.parkezz.payment.entity.PaymentMethod;
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

class PaymentRequestValidationTest {

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
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void nullBookingId_isRejected() {
        PaymentRequest request = new PaymentRequest(null, BigDecimal.valueOf(100), PaymentMethod.UPI);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Booking ID is required"));
    }

    @Test
    void nullAmount_isRejected() {
        PaymentRequest request = new PaymentRequest(1L, null, PaymentMethod.UPI);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Amount is required"));
    }

    @Test
    void zeroAmount_isRejected() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.ZERO, PaymentMethod.UPI);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("greater than zero"));
    }

    @Test
    void negativeAmount_isRejected() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(-1), PaymentMethod.UPI);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("greater than zero"));
    }

    @Test
    void boundaryAmount_oneCent_isAccepted() {
        PaymentRequest request = new PaymentRequest(1L, new BigDecimal("0.01"), PaymentMethod.UPI);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void nullPaymentMethod_isRejected() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), null);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Payment method is required"));
    }
}
