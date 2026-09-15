package com.ticketflow.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Event Service — owns events and their ticket inventory, exposes a REST API
 * consumed by Booking Service, and hosts the daily revenue Spring Batch job.
 *
 * <p>{@code @EnableScheduling} drives the daily trigger for the batch report.
 */
@SpringBootApplication
@EnableScheduling
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
