package com.parkezz.auth.client;

import com.parkezz.auth.dto.UserInternalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Talks to User Service over REST using its Eureka service-id ("user-service")
 * instead of a hardcoded host:port. The load-balanced RestTemplate resolves
 * "user-service" to an actual instance via Eureka at call time.
 */
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service/api/users/internal/by-email/";

    public UserInternalResponse getUserByEmail(String email) {
        return restTemplate.getForObject(USER_SERVICE_URL + email, UserInternalResponse.class);
    }
}
