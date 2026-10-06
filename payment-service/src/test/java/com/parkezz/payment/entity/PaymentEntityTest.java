package com.parkezz.payment.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentEntityTest {

    @Test
    void payment_noArgsConstructorAndSettersGetters() {
        Payment payment = new Payment();

        LocalDateTime date = LocalDateTime.now();

        payment.setPaymentId(1L);
        payment.setBookingId(2L);
        payment.setUserId(3L);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(date);

        assertThat(payment.getPaymentId()).isEqualTo(1L);
        assertThat(payment.getBookingId()).isEqualTo(2L);
        assertThat(payment.getUserId()).isEqualTo(3L);
        assertThat(payment.getAmount()).isEqualByComparingTo("100");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getPaymentDate()).isEqualTo(date);
    }

    @Test
    void payment_allArgsConstructor() {
        LocalDateTime date = LocalDateTime.now();

        Payment payment = new Payment(
                1L,
                2L,
                3L,
                BigDecimal.valueOf(200),
                PaymentMethod.CARD,
                PaymentStatus.SUCCESS,
                date
        );

        assertThat(payment.getPaymentId()).isEqualTo(1L);
        assertThat(payment.getBookingId()).isEqualTo(2L);
        assertThat(payment.getUserId()).isEqualTo(3L);
        assertThat(payment.getAmount()).isEqualByComparingTo("200");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getPaymentDate()).isEqualTo(date);
    }

    @Test
    void payment_builder_createsCorrectObject() {
        LocalDateTime date = LocalDateTime.now();

        Payment payment = Payment.builder()
                .paymentId(10L)
                .bookingId(20L)
                .userId(30L)
                .amount(BigDecimal.valueOf(500))
                .paymentMethod(PaymentMethod.NET_BANKING)
                .paymentStatus(PaymentStatus.FAILED)
                .paymentDate(date)
                .build();

        assertThat(payment.getPaymentId()).isEqualTo(10L);
        assertThat(payment.getBookingId()).isEqualTo(20L);
        assertThat(payment.getUserId()).isEqualTo(30L);
        assertThat(payment.getAmount()).isEqualByComparingTo("500");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.NET_BANKING);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getPaymentDate()).isEqualTo(date);
    }

    @Test
    void payment_equalsHashCodeAndToString() {
        LocalDateTime date = LocalDateTime.now();

        Payment payment1 = Payment.builder()
                .paymentId(1L)
                .bookingId(2L)
                .userId(3L)
                .amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(date)
                .build();

        Payment payment2 = Payment.builder()
                .paymentId(1L)
                .bookingId(2L)
                .userId(3L)
                .amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(date)
                .build();

        assertThat(payment1).isEqualTo(payment2);
        assertThat(payment1.hashCode()).isEqualTo(payment2.hashCode());
        assertThat(payment1.toString()).contains("paymentId");
    }

    @Test
    void paymentStatus_enumValues() {
        assertThat(PaymentStatus.values())
                .containsExactly(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
    }

    @Test
    void paymentMethod_enumValues() {
        assertThat(PaymentMethod.values())
                .containsExactly(
                        PaymentMethod.UPI,
                        PaymentMethod.CARD,
                        PaymentMethod.NET_BANKING
                );
    }
}