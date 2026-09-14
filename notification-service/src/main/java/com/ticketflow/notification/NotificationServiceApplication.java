package com.ticketflow.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service — consumes {@code BookingConfirmedEvent} from Kafka and
 * records a notification (mock "email"). It has no public REST API and is not
 * routed through the gateway; it is a pure event consumer.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
