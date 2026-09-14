package com.ticketflow.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TicketFlow — Booking Service API")
                .description("Create bookings: reserve tickets from Event Service and emit booking events.")
                .version("v1"));
    }
}
