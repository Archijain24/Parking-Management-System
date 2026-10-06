package com.parkezz.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Gateway application context starts and that the expected
 * routes (one per downstream microservice) are actually configured.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false"
})
class GatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void contextLoads() {
        assertThat(routeLocator).isNotNull();
    }

    @Test
    void allExpectedRoutesAreConfigured() {
        StepVerifier.create(routeLocator.getRoutes().collectList())
                .assertNext(routes -> {
                    // 6 routes now: user-service is split into "register" (public
                    // sign-up) and "profile" (GET/PUT/DELETE /api/users/{id}) since
                    // the profile route was missing entirely before this fix.
                    assertThat(routes).hasSize(6);
                    assertThat(routes).extracting(r -> r.getId())
                            .containsExactlyInAnyOrder(
                                    "user-service-register",
                                    "user-service-profile",
                                    "auth-service",
                                    "parking-service",
                                    "booking-service",
                                    "payment-service"
                            );
                })
                .verifyComplete();
    }
}
