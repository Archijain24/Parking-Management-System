package com.parkezz.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

/**
 * Auth / Login Service.
 *
 * Does NOT own or touch the users table/database directly. Instead it calls
 * User Service over REST (via Eureka + a load-balanced RestTemplate) to fetch
 * user details, verifies the password locally with BCrypt, and issues a JWT
 * on success. This keeps each microservice's data ownership clean.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
