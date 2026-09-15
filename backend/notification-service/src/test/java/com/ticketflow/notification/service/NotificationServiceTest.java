package com.ticketflow.notification.service;

import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.notification.domain.Notification;
import com.ticketflow.notification.domain.NotificationChannel;
import com.ticketflow.notification.domain.NotificationStatus;
import com.ticketflow.notification.email.EmailSender;
import com.ticketflow.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private NotificationService notificationService;

    private static final BookingConfirmedEvent EVENT = new BookingConfirmedEvent(
            1L, "BK-ABC12345", 1L, "Spring Boot Live 2026",
            "Alice", "alice@example.com", 2, new BigDecimal("198.00"), Instant.now());

    @Test
    void handle_savesNotification_forNewBooking() {
        when(notificationRepository.existsByBookingReference("BK-ABC12345")).thenReturn(false);

        notificationService.handleBookingConfirmed(EVENT);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getBookingReference()).isEqualTo("BK-ABC12345");
        assertThat(saved.getRecipientEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getMessage()).contains("BK-ABC12345", "Spring Boot Live 2026");

        // An email was sent to the customer with the booking reference in the body.
        verify(emailSender).send(eq("alice@example.com"), anyString(), contains("BK-ABC12345"));
    }

    @Test
    void handle_isIdempotent_whenBookingAlreadyProcessed() {
        when(notificationRepository.existsByBookingReference("BK-ABC12345")).thenReturn(true);

        notificationService.handleBookingConfirmed(EVENT);

        // Redelivered event must not create a duplicate notification or resend.
        verify(notificationRepository, never()).save(any());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}
