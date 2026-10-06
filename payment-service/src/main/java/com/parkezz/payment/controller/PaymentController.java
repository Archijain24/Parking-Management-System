package com.parkezz.payment.controller;

import com.parkezz.payment.dto.PaymentRequest;
import com.parkezz.payment.dto.PaymentResponse;
import com.parkezz.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Simulated payments for bookings")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Make a simulated payment for a booking (CUSTOMER only)")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request,
                                                Authentication authentication,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = Long.parseLong(authentication.getName());
        PaymentResponse response = paymentService.pay(request, userId, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get your own payment by ID")
    public PaymentResponse getById(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return paymentService.getById(id, userId);
    }
}
