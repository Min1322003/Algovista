package com.algovista.backend.lesson;

import com.algovista.backend.config.ApiException;
import com.algovista.backend.feedback.FeedbackAnalyzer;
import com.algovista.backend.feedback.FeedbackResult;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Coordinates the lesson lifecycle: start/resume, activity progression, submit.
 * Progress = number of activities completed = index of the current activity.
 */
@Service
public class LessonManager {

    private final LessonRepository repository;
    private final FeedbackAnalyzer feedbackAnalyzer;

    public LessonManager(LessonRepository repository, FeedbackAnalyzer feedbackAnalyzer) {
        this.repository = repository;
        this.feedbackAnalyzer = feedbackAnalyzer;
    }

    public List<LessonSummary> listLessons(String userId) {
        return repository.findSummaries(userId);
    }

    /** Starts the lesson, or returns the saved position if the learner already started it. */
    public LessonSession startLesson(String userId, String lessonId) {
        LessonInfo lesson = requireLesson(lessonId);
        if (repository.countActivities(lessonId) == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "This lesson has no activities yet");
        }
        repository.createProgressIfAbsent(userId, lessonId);
        return buildSession(userId, lesson);
    }

    /** Current state of a lesson that was already started (used after Continue or a page refresh). */
    public LessonSession resumeLesson(String userId, String lessonId) {
        return buildSession(userId, requireLesson(lessonId));
    }

    public LessonSession restartLesson(String userId, String lessonId) {
        LessonInfo lesson = requireLesson(lessonId);
        repository.createProgressIfAbsent(userId, lessonId);
        repository.resetProgress(userId, lessonId);
        return buildSession(userId, lesson);
    }

    public SubmitResponse submitAnswer(String userId, String lessonId, SubmitRequest request) {
        if (request == null || request.activityId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "activityId is required");
        }
        requireLesson(lessonId);
        LessonRepository.Progress progress = repository.findProgress(userId, lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Lesson not started"));

        int total = repository.countActivities(lessonId);
        int position = progress.currentPosition();
        if (position >= total) {
            throw new ApiException(HttpStatus.CONFLICT, "Lesson already completed");
        }

        Activity current = repository.findActivityAt(lessonId, position)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (current.id() != request.activityId()) {
            throw new ApiException(HttpStatus.CONFLICT, "Not the current activity, reload the lesson");
        }

        List<String> answer = request.answer() == null ? List.of() : request.answer();
        FeedbackResult result = feedbackAnalyzer.analyzeAnswer(current, answer);

        int completed = position;
        boolean lessonCompleted = false;
        if (result.correct()) {
            int next = position + 1;
            lessonCompleted = next >= total;
            ProgressStatus status = lessonCompleted ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS;
            if (!repository.advance(userId, lessonId, position, next, status)) {
                throw new ApiException(HttpStatus.CONFLICT, "Progress changed, reload the lesson");
            }
            completed = next;
        }
        // Wrong answer: nothing is saved. The learner retries the same activity.
        return new SubmitResponse(result, total, completed, lessonCompleted);
    }

    private LessonSession buildSession(String userId, LessonInfo lesson) {
        LessonRepository.Progress progress = repository.findProgress(userId, lesson.id())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not started"));

        int total = repository.countActivities(lesson.id());
        int completed = Math.min(progress.currentPosition(), total);
        boolean done = completed >= total;

        ActivityView current = done ? null
                : repository.findActivityAt(lesson.id(), completed)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Activity not found"))
                        .toView();

        return new LessonSession(lesson.id(), lesson.title(),
                done ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS,
                total, completed, current, repository.findActivityTitles(lesson.id()));
    }

    private LessonInfo requireLesson(String lessonId) {
        return repository.findLesson(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));
    }
}
