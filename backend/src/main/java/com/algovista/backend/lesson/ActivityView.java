package com.algovista.backend.lesson;

import com.fasterxml.jackson.annotation.JsonRawValue;

/** What the client is allowed to see. content is JSON stored in the database, passed through as-is. */
public record ActivityView(
        long id,
        int position,
        ActivityType type,
        String title,
        String instruction,
        @JsonRawValue String content) {
}
