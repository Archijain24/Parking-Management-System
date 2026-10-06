package com.parkezz.payment.service;

import com.parkezz.payment.client.BookingServiceClient;
import com.parkezz.payment.dto.BookingResponse;
import com.parkezz.payment.dto.PaymentRequest;
import com.parkezz.payment.dto.PaymentResponse;
import com.parkezz.payment.entity.Payment;
import com.parkezz.payment.entity.PaymentMethod;
import com.parkezz.payment.entity.PaymentStatus;
import com.parkezz.payment.exception.PaymentFailedException;
import com.parkezz.payment.exception.PaymentNotFoundException;
import com.parkezz.payment.exception.UnauthorizedActionException;
import com.parkezz.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingServiceClient bookingServiceClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private BookingResponse booking(Long bookingId, Long userId, BigDecimal amount) {
        return new BookingResponse(bookingId, userId, 10L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                amount, "PENDING");
    }

    @Test
    void pay_validCustomerAndAmount_resultsInSuccessAndConfirmsBooking() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token"))
                .thenReturn(booking(1L, 100L, BigDecimal.valueOf(100)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(1L);
            return p;
        });

        PaymentResponse response = paymentService.pay(request, 100L, "Bearer test-token");

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(bookingServiceClient).getBooking(1L, "Bearer test-token");
        verify(bookingServiceClient).confirmBooking(1L, "Bearer test-token");
    }

    @Test
    void pay_amountDoesNotMatchBooking_throwsPaymentFailedException() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(90), PaymentMethod.UPI);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token"))
                .thenReturn(booking(1L, 100L, BigDecimal.valueOf(100)));

        assertThatThrownBy(() -> paymentService.pay(request, 100L, "Bearer test-token"))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessage("Payment amount does not match booking amount");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingServiceClient, never()).confirmBooking(anyLong(), anyString());
    }

    @Test
    void pay_bookingBelongsToAnotherUser_throwsUnauthorizedActionException() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token"))
                .thenReturn(booking(1L, 999L, BigDecimal.valueOf(100)));

        assertThatThrownBy(() -> paymentService.pay(request, 100L, "Bearer test-token"))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessage("You cannot pay for another user's booking");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingServiceClient, never()).confirmBooking(anyLong(), anyString());
    }

    @Test
    void pay_bookingNotFound_throwsNotFoundException() {
        PaymentRequest request = new PaymentRequest(
                99L,
                BigDecimal.valueOf(100),
                PaymentMethod.UPI
        );

        when(bookingServiceClient.getBooking(99L, "Bearer test-token"))
                .thenThrow(
                        new org.springframework.web.client.HttpClientErrorException(
                                org.springframework.http.HttpStatus.NOT_FOUND
                        )
                );

        assertThatThrownBy(() ->
                paymentService.pay(request, 100L, "Bearer test-token")
        )
                .isInstanceOf(org.springframework.web.client.HttpClientErrorException.class)
                .hasMessageContaining("404 NOT_FOUND");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingServiceClient, never())
                .confirmBooking(anyLong(), anyString());
    }

    @Test
    void pay_nullBookingResponse_throwsPaymentFailedException() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token")).thenReturn(null);

        assertThatThrownBy(() -> paymentService.pay(request, 100L, "Bearer test-token"))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessage("Booking not found with id: 1");
    }

    @Test
    void pay_validBookingWithZeroAmount_canExerciseDefensiveFailedPath() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.ZERO, PaymentMethod.CARD);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token"))
                .thenReturn(booking(1L, 100L, BigDecimal.ZERO));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(2L);
            return p;
        });

        PaymentResponse response = paymentService.pay(request, 100L, "Bearer test-token");

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingServiceClient, never()).confirmBooking(anyLong(), anyString());
    }

    @Test
    void pay_negativeAmount_canExerciseDefensiveFailedPath() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(-5), PaymentMethod.CARD);
        when(bookingServiceClient.getBooking(1L, "Bearer test-token"))
                .thenReturn(booking(1L, 100L, BigDecimal.valueOf(-5)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(3L);
            return p;
        });

        PaymentResponse response = paymentService.pay(request, 100L, "Bearer test-token");

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingServiceClient, never()).confirmBooking(anyLong(), anyString());
    }

    @Test
    void pay_paymentSavedWithCorrectUserIdAndBookingId() {
        PaymentRequest request = new PaymentRequest(7L, BigDecimal.valueOf(200), PaymentMethod.NET_BANKING);
        when(bookingServiceClient.getBooking(7L, "Bearer test-token"))
                .thenReturn(booking(7L, 55L, BigDecimal.valueOf(200)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.pay(request, 55L, "Bearer test-token");

        assertThat(response.getUserId()).isEqualTo(55L);
        assertThat(response.getBookingId()).isEqualTo(7L);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.NET_BANKING);
    }

    @Test
    void getById_paymentExists_returnsResponse() {
        Payment payment = Payment.builder().paymentId(1L).bookingId(1L).userId(100L)
                .amount(BigDecimal.valueOf(50)).paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS).paymentDate(LocalDateTime.now()).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getById(1L, 100L);

        assertThat(response.getPaymentId()).isEqualTo(1L);
    }

    @Test
    void getById_paymentNotFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getById(99L, 100L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getById_anotherUsersPayment_throwsUnauthorizedActionException() {
        Payment payment = Payment.builder().paymentId(1L).bookingId(1L).userId(100L)
                .amount(BigDecimal.valueOf(50)).paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS).paymentDate(LocalDateTime.now()).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getById(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }
}
