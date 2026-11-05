package com.algovista.backend.lesson;

import java.util.List;

public record SubmitRequest(Long activityId, List<String> answer) {
}
