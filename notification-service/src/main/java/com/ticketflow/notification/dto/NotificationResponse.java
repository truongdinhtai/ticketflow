package com.ticketflow.notification.dto;

import com.ticketflow.notification.domain.Notification;
import com.ticketflow.notification.domain.NotificationChannel;
import com.ticketflow.notification.domain.NotificationStatus;

import java.time.Instant;

/** API view of a {@link Notification}. */
public record NotificationResponse(
        Long id,
        String bookingReference,
        String recipientEmail,
        NotificationChannel channel,
        NotificationStatus status,
        String message,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getBookingReference(),
                n.getRecipientEmail(),
                n.getChannel(),
                n.getStatus(),
                n.getMessage(),
                n.getCreatedAt());
    }
}
