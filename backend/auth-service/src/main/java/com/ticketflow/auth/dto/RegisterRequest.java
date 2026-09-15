package com.ticketflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 100, message = "password must be 6-100 characters")
        String password,

        @NotBlank
        @Size(max = 100)
        String displayName
) {
}
