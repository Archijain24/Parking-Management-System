package com.parkezz.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - Service Registry for all Parkezz microservices.
 *
 * Every microservice (Gateway, User, Auth, Parking, Booking, Payment) registers
 * itself here on startup. Other services then ask Eureka "where is user-service?"
 * instead of using a hardcoded host/port. This is called Service Discovery.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
