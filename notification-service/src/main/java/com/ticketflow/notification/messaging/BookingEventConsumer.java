package com.ticketflow.notification.messaging;

import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.common.event.KafkaTopics;
import com.ticketflow.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code booking.confirmed} events and turns each into a notification.
 * The consumer group id comes from configuration
 * ({@code spring.kafka.consumer.group-id}).
 */
@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final NotificationService notificationService;

    public BookingEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CONFIRMED)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent for booking {} (event {})",
                event.bookingReference(), event.eventId());
        notificationService.handleBookingConfirmed(event);
    }
}
