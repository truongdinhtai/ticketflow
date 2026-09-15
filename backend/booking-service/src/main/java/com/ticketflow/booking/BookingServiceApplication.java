package com.ticketflow.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Booking Service — accepts ticket bookings. For each booking it reserves
 * tickets from Event Service over REST (Feign, guarded by Resilience4j),
 * persists the booking, and publishes a {@code BookingConfirmedEvent} to Kafka
 * for Notification Service.
 *
 * <p>{@code @EnableAsync} lets event publishing run off the request thread, so a
 * slow or absent broker never delays the booking response.
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableCaching
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
