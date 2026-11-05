package com.algovista.backend.feedback;

/** Only the fields relevant to the outcome are filled; the others are null. */
public record Feedback(String praiseMessage, String clueMessage, String explanation) {
}
