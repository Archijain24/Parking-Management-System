package com.parkezz.payment.service;

import com.parkezz.payment.client.BookingServiceClient;
import com.parkezz.payment.dto.BookingResponse;
import com.parkezz.payment.dto.PaymentRequest;
import com.parkezz.payment.dto.PaymentResponse;
import com.parkezz.payment.entity.Payment;
import com.parkezz.payment.entity.PaymentStatus;
import com.parkezz.payment.exception.PaymentFailedException;
import com.parkezz.payment.exception.PaymentNotFoundException;
import com.parkezz.payment.exception.UnauthorizedActionException;
import com.parkezz.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment is simulated - there is no real payment gateway. The simulation
 * rule is intentionally simple and DETERMINISTIC (not random) so the flow is
 * repeatable and demo/viva-friendly:
 *
 *   amount > 0  -> SUCCESS -> Booking Service is called to CONFIRM the booking
 *   amount <= 0 -> would already be rejected by @DecimalMin validation, but
 *                  as a defensive second layer, non-positive amounts are
 *                  still treated as FAILED here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;

    @Override
    public PaymentResponse pay(PaymentRequest request, Long userId, String authorizationHeader) {
        log.info("Payment request received: userId={}, bookingId={}, amount={}",
                userId, request.getBookingId(), request.getAmount());

        getAndValidateBooking(request.getBookingId(), userId, request.getAmount(), authorizationHeader);

        PaymentStatus status = simulatePayment(request.getAmount());

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(userId)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(status)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        if (status == PaymentStatus.SUCCESS) {
            log.info("Payment processed successfully: paymentId={}", saved.getPaymentId());
            bookingServiceClient.confirmBooking(request.getBookingId(), authorizationHeader);
        } else {
            log.warn("Payment failed: paymentId={}, bookingId={}", saved.getPaymentId(), request.getBookingId());
        }

        return toResponse(saved);
    }

    @Override
    public PaymentResponse getById(Long id, Long userId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You cannot view another user's payment");
        }
        return toResponse(payment);
    }

    private BookingResponse getAndValidateBooking(Long bookingId, Long userId, BigDecimal amount,
                                                   String authorizationHeader) {
        try {
            BookingResponse booking = bookingServiceClient.getBooking(bookingId, authorizationHeader);

            if (booking == null) {
                throw new PaymentFailedException("Booking not found with id: " + bookingId);
            }

            if (!userId.equals(booking.getUserId())) {
                throw new UnauthorizedActionException("You cannot pay for another user's booking");
            }

            if (booking.getTotalAmount() == null || amount.compareTo(booking.getTotalAmount()) != 0) {
                throw new PaymentFailedException("Payment amount does not match booking amount");
            }

            return booking;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new PaymentFailedException("Booking not found with id: " + bookingId);
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden ex) {
            throw new UnauthorizedActionException("You cannot pay for another user's booking");
        }
    }

    private PaymentStatus simulatePayment(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
