package com.ticketflow.notification;

import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.common.event.KafkaTopics;
import com.ticketflow.notification.domain.Notification;
import com.ticketflow.notification.repository.NotificationRepository;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end consumer test: publish a {@link BookingConfirmedEvent} to an
 * embedded Kafka broker and assert Notification Service consumes it and records
 * a notification in a Testcontainers PostgreSQL database.
 *
 * <p>Requires Docker (Testcontainers). Named {@code *IT} so it runs in the
 * Failsafe {@code verify} phase.
 */
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        // Mail bean must exist; point at a dead port with a fast timeout so the
        // email attempt fails quickly (recorded as FAILED). This IT verifies the
        // Kafka consume -> persist path, not delivery.
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.properties.mail.smtp.connectiontimeout=500",
        "spring.mail.properties.mail.smtp.timeout=500",
        // Consumer config normally comes from Config Server; supply it here.
        "spring.kafka.consumer.group-id=notification-service-it",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=com.ticketflow.common.event",
        "spring.kafka.consumer.properties.spring.json.use.type.headers=false",
        "spring.kafka.consumer.properties.spring.json.value.default.type=com.ticketflow.common.event.BookingConfirmedEvent"
})
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.BOOKING_CONFIRMED})
class NotificationConsumerIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void consumesBookingEvent_andRecordsNotification() {
        var event = new BookingConfirmedEvent(1L, "BK-IT000001", 1L, "Spring Boot Live 2026",
                "Alice", "alice@example.com", 2, new BigDecimal("198.00"), Instant.now());

        kafkaTemplate().send(KafkaTopics.BOOKING_CONFIRMED, event.bookingReference(), event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(notificationRepository.existsByBookingReference("BK-IT000001")).isTrue());

        Notification saved = notificationRepository.findAll().stream()
                .filter(n -> n.getBookingReference().equals("BK-IT000001"))
                .findFirst().orElseThrow();
        assertThat(saved.getRecipientEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getMessage()).contains("Spring Boot Live 2026");
    }

    private KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafka));
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", JsonSerializer.class);
        ProducerFactory<String, BookingConfirmedEvent> pf = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(pf);
    }
}
