package com.ticketflow.notification.repository;

import com.ticketflow.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByBookingReference(String bookingReference);
}
