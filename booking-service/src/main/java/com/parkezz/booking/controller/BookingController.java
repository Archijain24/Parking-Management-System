package com.parkezz.booking.controller;

import com.parkezz.booking.dto.BookingRequest;
import com.parkezz.booking.dto.BookingResponse;
import com.parkezz.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create, view and cancel bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking (CUSTOMER only)")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                   Authentication authentication,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = Long.parseLong(authentication.getName());
        BookingResponse response = bookingService.create(request, userId, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID (only your own)")
    public BookingResponse getById(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return bookingService.getById(id, userId);
    }

    @GetMapping("/my")
    @Operation(summary = "Get all bookings belonging to the current user")
    public List<BookingResponse> getMyBookings(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return bookingService.getMyBookings(userId);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel your own booking")
    public BookingResponse cancel(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return bookingService.cancel(id, userId);
    }

    /**
     * INTERNAL endpoint called by Payment Service after a payment succeeds.
     * Not part of the normal customer-facing flow.
     */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "[Internal] Confirm a booking after successful payment")
    public BookingResponse confirm(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return bookingService.confirm(id, userId);
    }
}
