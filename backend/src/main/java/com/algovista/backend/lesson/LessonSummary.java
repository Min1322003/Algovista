package com.algovista.backend.lesson;

public record LessonSummary(
        String id,
        String title,
        String description,
        int totalActivities,
        int completedActivities,
        ProgressStatus status) {
}
