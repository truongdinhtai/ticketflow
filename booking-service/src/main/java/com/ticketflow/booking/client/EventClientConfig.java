package com.ticketflow.booking.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Per-client Feign configuration for {@link EventServiceClient}. Not annotated
 * with {@code @Configuration} on purpose, so it is applied only to this client
 * (referenced from the {@code @FeignClient} annotation) rather than globally.
 */
public class EventClientConfig {

    @Bean
    public ErrorDecoder eventServiceErrorDecoder() {
        return new EventServiceErrorDecoder();
    }
}
