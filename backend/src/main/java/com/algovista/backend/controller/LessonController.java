package com.algovista.backend.controller;

import com.algovista.backend.config.ApiException;
import com.algovista.backend.lesson.LessonManager;
import com.algovista.backend.lesson.LessonSession;
import com.algovista.backend.lesson.LessonSummary;
import com.algovista.backend.lesson.SubmitRequest;
import com.algovista.backend.lesson.SubmitResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The learner is identified by the X-User-Id header for now (anonymous id from the browser).
 * Part 7 replaces this with the logged-in user from the JWT.
 */
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonManager lessonManager;

    public LessonController(LessonManager lessonManager) {
        this.lessonManager = lessonManager;
    }

    @GetMapping
    public List<LessonSummary> list(@RequestHeader("X-User-Id") String userId) {
        return lessonManager.listLessons(validUser(userId));
    }

    @PostMapping("/{lessonId}/start")
    public LessonSession start(@RequestHeader("X-User-Id") String userId, @PathVariable String lessonId) {
        return lessonManager.startLesson(validUser(userId), lessonId);
    }

    @GetMapping("/{lessonId}/session")
    public LessonSession session(@RequestHeader("X-User-Id") String userId, @PathVariable String lessonId) {
        return lessonManager.resumeLesson(validUser(userId), lessonId);
    }

    @PostMapping("/{lessonId}/submit")
    public SubmitResponse submit(@RequestHeader("X-User-Id") String userId, @PathVariable String lessonId,
            @RequestBody SubmitRequest request) {
        return lessonManager.submitAnswer(validUser(userId), lessonId, request);
    }

    @PostMapping("/{lessonId}/restart")
    public LessonSession restart(@RequestHeader("X-User-Id") String userId, @PathVariable String lessonId) {
        return lessonManager.restartLesson(validUser(userId), lessonId);
    }

    private String validUser(String userId) {
        String trimmed = userId == null ? "" : userId.trim();
        if (trimmed.isEmpty() || trimmed.length() > 64) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "X-User-Id must be 1-64 characters");
        }
        return trimmed;
    }
}
