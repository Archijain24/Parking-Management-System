package com.parkezz.booking.client;

import com.parkezz.booking.dto.ParkingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Calls Parking Service (by its Eureka service-id "parking-service") to
 * verify a parking space exists and is available before creating a booking.
 *
 * Parking Service's GET endpoint requires an authenticated JWT, so the
 * caller's own token (already validated by this service's JwtFilter) is
 * forwarded on the outgoing internal call. This keeps Parking Service's
 * security consistent without needing a separate internal-only client id.
 */
@Component
@RequiredArgsConstructor
public class ParkingServiceClient {

    private final RestTemplate restTemplate;

    private static final String PARKING_SERVICE_URL = "http://parking-service/api/parking/";

    public ParkingResponse getParkingById(Long parkingId, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                PARKING_SERVICE_URL + parkingId,
                HttpMethod.GET,
                entity,
                ParkingResponse.class
        ).getBody();
    }
}
