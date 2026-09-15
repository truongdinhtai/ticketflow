package com.ticketflow.booking.service;

import com.ticketflow.booking.client.EventReservationGateway;
import com.ticketflow.booking.client.ReservationResponse;
import com.ticketflow.booking.domain.Booking;
import com.ticketflow.booking.domain.BookingStatus;
import com.ticketflow.booking.dto.BookingResponse;
import com.ticketflow.booking.dto.CreateBookingRequest;
import com.ticketflow.booking.exception.EventReservationRejectedException;
import com.ticketflow.booking.messaging.BookingEventPublisher;
import com.ticketflow.booking.repository.BookingRepository;
import com.ticketflow.common.event.BookingConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the booking orchestration logic. Collaborators are mocked, so
 * these run fast with no database, Kafka, or Docker.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private EventReservationGateway eventReservationGateway;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingEventPublisher eventPublisher;
    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private BookingService bookingService;

    private static final CreateBookingRequest REQUEST =
            new CreateBookingRequest(1L, "Alice", "alice@example.com", 2);

    @Test
    void createBooking_reserves_persists_andPublishesEvent() {
        var reservation = new ReservationResponse(1L, "Spring Boot Live 2026", 2,
                new BigDecimal("99.00"), new BigDecimal("198.00"));
        when(eventReservationGateway.reserve(1L, 2)).thenReturn(reservation);
        // Repository returns the entity it was asked to save.
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking(REQUEST);

        // The reservation drove the booking's denormalised fields.
        assertThat(response.eventName()).isEqualTo("Spring Boot Live 2026");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualByComparingTo("99.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("198.00");
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.bookingReference()).startsWith("BK-");

        // Exactly one event published, carrying the correct data.
        ArgumentCaptor<BookingConfirmedEvent> captor = ArgumentCaptor.forClass(BookingConfirmedEvent.class);
        verify(eventPublisher).publishBookingConfirmed(captor.capture());
        BookingConfirmedEvent event = captor.getValue();
        assertThat(event.eventId()).isEqualTo(1L);
        assertThat(event.customerEmail()).isEqualTo("alice@example.com");
        assertThat(event.quantity()).isEqualTo(2);
        assertThat(event.totalAmount()).isEqualByComparingTo("198.00");
        assertThat(event.bookingReference()).startsWith("BK-");
    }

    @Test
    void createBooking_whenEventServiceRejects_doesNotPersistOrPublish() {
        when(eventReservationGateway.reserve(1L, 2))
                .thenThrow(new EventReservationRejectedException("sold out"));

        assertThatThrownBy(() -> bookingService.createBooking(REQUEST))
                .isInstanceOf(EventReservationRejectedException.class);

        // No booking saved and no event published when the reservation fails.
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishBookingConfirmed(any());
    }
}
