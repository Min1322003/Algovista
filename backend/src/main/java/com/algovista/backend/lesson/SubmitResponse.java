package com.algovista.backend.lesson;

import com.algovista.backend.feedback.FeedbackResult;

public record SubmitResponse(
        FeedbackResult result,
        int totalActivities,
        int completedActivities,
        boolean lessonCompleted) {
}
