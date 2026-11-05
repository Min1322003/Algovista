package com.algovista.backend.lesson;

import java.util.List;

/** Everything the lesson screen needs. currentActivity is null once the lesson is completed. */
public record LessonSession(
        String lessonId,
        String title,
        ProgressStatus status,
        int totalActivities,
        int completedActivities,
        ActivityView currentActivity,
        List<String> activityTitles) {
}
