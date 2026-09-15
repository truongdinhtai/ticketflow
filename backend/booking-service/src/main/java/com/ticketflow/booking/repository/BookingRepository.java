package com.ticketflow.booking.repository;

import com.ticketflow.booking.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    /** Only the given customer's bookings (privacy: users see only their own). */
    Page<Booking> findByCustomerEmail(String customerEmail, Pageable pageable);
}
