package com.ticketflow.notification.controller;

import com.ticketflow.notification.dto.NotificationResponse;
import com.ticketflow.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only endpoint to inspect the notifications this service has recorded.
 * Handy for demos/tests; this service is otherwise a pure Kafka consumer and is
 * not exposed through the API gateway.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Inspect recorded notifications (read-only)")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    @Operation(summary = "List recorded notifications (paged)")
    public Page<NotificationResponse> list(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(NotificationResponse::from);
    }
}
