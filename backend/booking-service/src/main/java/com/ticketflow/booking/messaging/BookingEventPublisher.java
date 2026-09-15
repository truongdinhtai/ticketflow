package com.ticketflow.booking.messaging;

import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.common.event.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publishes booking domain events to Kafka. Sending is asynchronous and does not
 * block (or roll back) the booking transaction: the booking is already durably
 * persisted, and Notification Service is eventually consistent. Delivery success
 * or failure is logged via the returned future.
 */
@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    public BookingEventPublisher(KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        // Key by booking reference so events for the same booking keep order.
        kafkaTemplate.send(KafkaTopics.BOOKING_CONFIRMED, event.bookingReference(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BookingConfirmedEvent for {}: {}",
                                event.bookingReference(), ex.getMessage());
                    } else {
                        log.info("Published BookingConfirmedEvent for {} to topic {}",
                                event.bookingReference(), KafkaTopics.BOOKING_CONFIRMED);
                    }
                });
    }
}
