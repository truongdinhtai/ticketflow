package com.ticketflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway — the single entry point into the platform. Routes are
 * defined declaratively in {@code config-repo/api-gateway.yml} and target
 * services by their Eureka name via client-side load balancing ({@code lb://}).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
