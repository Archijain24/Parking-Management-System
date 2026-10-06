package com.parkezz.payment.client;

import com.parkezz.payment.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Calls Booking Service to validate a booking and confirm it after payment.
 */
@Component
@RequiredArgsConstructor
public class BookingServiceClient {

    private final RestTemplate restTemplate;

    private static final String BOOKING_SERVICE_URL = "http://booking-service/api/bookings/";

    public BookingResponse getBooking(Long bookingId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<BookingResponse> response = restTemplate.exchange(
                BOOKING_SERVICE_URL + bookingId,
                org.springframework.http.HttpMethod.GET,
                entity,
                BookingResponse.class);
        return response.getBody();
    }

    public void confirmBooking(Long bookingId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        restTemplate.put(BOOKING_SERVICE_URL + bookingId + "/confirm", new HttpEntity<>(headers));
    }
}
