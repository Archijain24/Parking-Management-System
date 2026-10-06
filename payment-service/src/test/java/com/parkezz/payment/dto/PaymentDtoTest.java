package com.parkezz.payment.dto;

import com.parkezz.payment.entity.PaymentMethod;
import com.parkezz.payment.entity.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentDtoTest {

    @Test
    void bookingResponse_noArgsConstructorAndSettersGetters() {
        BookingResponse response = new BookingResponse();

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);
        BigDecimal amount = BigDecimal.valueOf(100);

        response.setBookingId(1L);
        response.setUserId(10L);
        response.setParkingId(20L);
        response.setStartTime(start);
        response.setEndTime(end);
        response.setTotalAmount(amount);
        response.setStatus("PENDING");

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getParkingId()).isEqualTo(20L);
        assertThat(response.getStartTime()).isEqualTo(start);
        assertThat(response.getEndTime()).isEqualTo(end);
        assertThat(response.getTotalAmount()).isEqualTo(amount);
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void bookingResponse_allArgsConstructorAndEqualsHashCode() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);
        BigDecimal amount = BigDecimal.valueOf(100);

        BookingResponse response1 = new BookingResponse(
                1L, 10L, 20L, start, end, amount, "PENDING"
        );

        BookingResponse response2 = new BookingResponse(
                1L, 10L, 20L, start, end, amount, "PENDING"
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("bookingId");
    }

    @Test
    void errorResponse_gettersSettersAndBuilder() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(404)
                .message("Not Found")
                .path("/api/payments/1")
                .build();

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Not Found");
        assertThat(response.getPath()).isEqualTo("/api/payments/1");

        response.setStatus(500);
        response.setMessage("Server Error");

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Server Error");
        assertThat(response.toString()).contains("Server Error");
    }

    @Test
    void errorResponse_allArgsConstructorAndEqualsHashCode() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response1 = new ErrorResponse(
                timestamp, 400, "Bad Request", "/test"
        );

        ErrorResponse response2 = new ErrorResponse(
                timestamp, 400, "Bad Request", "/test"
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void paymentRequest_noArgsConstructorAndSettersGetters() {
        PaymentRequest request = new PaymentRequest();

        request.setBookingId(1L);
        request.setAmount(BigDecimal.valueOf(150));
        request.setPaymentMethod(PaymentMethod.UPI);

        assertThat(request.getBookingId()).isEqualTo(1L);
        assertThat(request.getAmount()).isEqualByComparingTo("150");
        assertThat(request.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(request.toString()).contains("bookingId");
    }

    @Test
    void paymentRequest_allArgsConstructorAndEqualsHashCode() {
        PaymentRequest request1 =
                new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.CARD);

        PaymentRequest request2 =
                new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.CARD);

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void paymentResponse_builderAndGetters() {
        LocalDateTime date = LocalDateTime.now();

        PaymentResponse response = PaymentResponse.builder()
                .paymentId(1L)
                .bookingId(2L)
                .userId(10L)
                .amount(BigDecimal.valueOf(250))
                .paymentMethod(PaymentMethod.NET_BANKING)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(date)
                .build();

        assertThat(response.getPaymentId()).isEqualTo(1L);
        assertThat(response.getBookingId()).isEqualTo(2L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getAmount()).isEqualByComparingTo("250");
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.NET_BANKING);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getPaymentDate()).isEqualTo(date);
    }

    @Test
    void paymentResponse_noArgsAndSetters() {
        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(5L);
        response.setBookingId(6L);
        response.setUserId(7L);
        response.setAmount(BigDecimal.valueOf(300));
        response.setPaymentMethod(PaymentMethod.UPI);
        response.setPaymentStatus(PaymentStatus.FAILED);

        LocalDateTime date = LocalDateTime.now();
        response.setPaymentDate(date);

        assertThat(response.getPaymentId()).isEqualTo(5L);
        assertThat(response.getBookingId()).isEqualTo(6L);
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getAmount()).isEqualByComparingTo("300");
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getPaymentDate()).isEqualTo(date);
    }

    @Test
    void paymentResponse_allArgsConstructorEqualsHashCode() {
        LocalDateTime date = LocalDateTime.now();

        PaymentResponse response1 = new PaymentResponse(
                1L, 2L, 3L,
                BigDecimal.valueOf(100),
                PaymentMethod.CARD,
                PaymentStatus.SUCCESS,
                date
        );

        PaymentResponse response2 = new PaymentResponse(
                1L, 2L, 3L,
                BigDecimal.valueOf(100),
                PaymentMethod.CARD,
                PaymentStatus.SUCCESS,
                date
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
}