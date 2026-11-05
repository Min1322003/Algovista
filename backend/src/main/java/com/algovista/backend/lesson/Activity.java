package com.algovista.backend.lesson;

import java.util.List;

/** Full activity as stored. Never sent to the client directly because it contains the answer. */
public record Activity(
        long id,
        int position,
        ActivityType type,
        String title,
        String instruction,
        String content,
        List<String> answer,
        String praiseMessage,
        String clueMessage,
        String explanation) {

    public ActivityView toView() {
        return new ActivityView(id, position, type, title, instruction, content);
    }
}
