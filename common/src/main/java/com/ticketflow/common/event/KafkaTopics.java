package com.ticketflow.common.event;

/**
 * Single source of truth for Kafka topic names shared between the producer
 * (Booking Service) and consumer (Notification Service). Keeping this in the
 * common module prevents the two sides from silently drifting apart.
 */
public final class KafkaTopics {

    /** Emitted by Booking Service when a booking is successfully confirmed. */
    public static final String BOOKING_CONFIRMED = "booking.confirmed";

    private KafkaTopics() {
        // utility holder, not instantiable
    }
}
