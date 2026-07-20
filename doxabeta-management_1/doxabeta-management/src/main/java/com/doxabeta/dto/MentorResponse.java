package com.doxabeta.dto;

import com.doxabeta.entity.Mentor;

/**
 * What a Mentor looks like over the API. Flat and simple — Mentor has no
 * associations that need flattening the way Student does.
 */
public record MentorResponse(
        Long id,
        String code,
        String name,
        String email
) {
    /** Converts a JPA entity into this response shape. */
    public static MentorResponse from(Mentor m) {
        return new MentorResponse(m.getId(), m.getCode(), m.getName(), m.getEmail());
    }
}
