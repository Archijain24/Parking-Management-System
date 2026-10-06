package com.parkezz.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - the single entry point for all client traffic.
 *
 * Clients (Postman/Swagger/browser) only ever talk to the Gateway on port 8080.
 * The Gateway looks up the target microservice's location in Eureka (service
 * discovery) and forwards ("routes") the request to it. This means clients
 * never need to know the internal ports of individual services, and services
 * can move/scale without clients noticing.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
