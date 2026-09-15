package com.ticketflow.booking;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketflow.booking.client.EventReservationGateway;
import com.ticketflow.booking.client.ReservationResponse;
import com.ticketflow.booking.dto.BookingResponse;
import com.ticketflow.booking.dto.CreateBookingRequest;
import com.ticketflow.booking.repository.BookingRepository;
import com.ticketflow.common.event.BookingConfirmedEvent;
import com.ticketflow.common.event.KafkaTopics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Full-stack integration test for the booking flow:
 * <ul>
 *   <li>a real PostgreSQL via Testcontainers (Flyway migrates the schema),</li>
 *   <li>an in-JVM Kafka broker via {@code @EmbeddedKafka},</li>
 *   <li>the Event Service call mocked (no need to run that service here).</li>
 * </ul>
 * It asserts that POST /api/bookings persists a booking AND publishes a
 * {@link BookingConfirmedEvent}.
 *
 * <p>Requires Docker to be available (Testcontainers). Named {@code *IT} so it
 * runs in the Failsafe {@code verify} phase, not with the fast unit tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cache.type=none"
})
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.BOOKING_CONFIRMED})
class BookingApiIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Point Kafka at the embedded broker started by @EmbeddedKafka.
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    // Mock the downstream Event Service call.
    @MockBean
    private EventReservationGateway eventReservationGateway;

    @Test
    void createBooking_persistsAndPublishesEvent() throws Exception {
        when(eventReservationGateway.reserve(eq(1L), eq(2)))
                .thenReturn(new ReservationResponse(1L, "Spring Boot Live 2026", 2,
                        new BigDecimal("99.00"), new BigDecimal("198.00")));

        var request = new CreateBookingRequest(1L, "Alice", "alice@example.com", 2);
        ResponseEntity<BookingResponse> response =
                rest.postForEntity("/api/bookings", request, BookingResponse.class);

        // 1) HTTP + persistence
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        String reference = response.getBody().bookingReference();
        assertThat(bookingRepository.findByBookingReference(reference)).isPresent();

        // 2) The event was published to Kafka
        BookingConfirmedEvent event = consumeOnePublishedEvent();
        assertThat(event.bookingReference()).isEqualTo(reference);
        assertThat(event.eventId()).isEqualTo(1L);
        assertThat(event.quantity()).isEqualTo(2);
        assertThat(event.totalAmount()).isEqualByComparingTo("198.00");
    }

    private BookingConfirmedEvent consumeOnePublishedEvent() throws Exception {
        Map<String, Object> props = KafkaTestUtils.consumerProps("it-consumer", "true", embeddedKafka);
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, KafkaTopics.BOOKING_CONFIRMED);
            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, KafkaTopics.BOOKING_CONFIRMED);
            JsonMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
            return mapper.readValue(record.value(), BookingConfirmedEvent.class);
        }
    }
}
