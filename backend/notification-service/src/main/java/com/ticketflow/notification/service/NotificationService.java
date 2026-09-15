package com.ticketflow.notification.service;

import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.notification.domain.Notification;
import com.ticketflow.notification.domain.NotificationChannel;
import com.ticketflow.notification.domain.NotificationStatus;
import com.ticketflow.notification.email.EmailSender;
import com.ticketflow.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public NotificationService(NotificationRepository notificationRepository, EmailSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    /**
     * Handle a booking event: send a confirmation email and record it.
     * Idempotent — a redelivered event for a booking already processed is
     * skipped, so at-least-once Kafka delivery cannot send duplicate emails.
     *
     * <p>If sending fails (e.g. SMTP down), the notification is still recorded
     * with status {@code FAILED} so there is an audit trail; the event is not
     * re-thrown, so the Kafka offset advances rather than looping forever.
     */
    @Transactional
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        if (notificationRepository.existsByBookingReference(event.bookingReference())) {
            log.info("Notification for booking {} already exists — skipping (idempotent)",
                    event.bookingReference());
            return;
        }

        String subject = "Your TicketFlow booking %s is confirmed".formatted(event.bookingReference());
        String body = buildBody(event);

        NotificationStatus status;
        try {
            emailSender.send(event.customerEmail(), subject, body);
            status = NotificationStatus.SENT;
            log.info("Sent confirmation email to {} for booking {}",
                    event.customerEmail(), event.bookingReference());
        } catch (Exception ex) {
            status = NotificationStatus.FAILED;
            log.error("Failed to send email to {} for booking {}: {}",
                    event.customerEmail(), event.bookingReference(), ex.getMessage());
        }

        notificationRepository.save(new Notification(
                event.bookingReference(),
                event.customerEmail(),
                NotificationChannel.EMAIL,
                status,
                body));
    }

    private String buildBody(BookingConfirmedEvent event) {
        return "Hi %s, your booking %s for '%s' is confirmed: %d ticket(s), total %s. See you there!"
                .formatted(event.customerName(), event.bookingReference(), event.eventName(),
                        event.quantity(), event.totalAmount());
    }
}
