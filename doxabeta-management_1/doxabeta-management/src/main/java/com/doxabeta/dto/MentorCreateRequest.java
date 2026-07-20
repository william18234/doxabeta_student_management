package com.doxabeta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Expected JSON body for POST /api/mentors. */
public record MentorCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @Email String email
) {
}
