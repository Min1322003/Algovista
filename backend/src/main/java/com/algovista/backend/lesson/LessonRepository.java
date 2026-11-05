package com.algovista.backend.lesson;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LessonRepository {

    public record Progress(int currentPosition, ProgressStatus status) {
    }

    private final JdbcClient jdbc;

    public LessonRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<LessonSummary> findSummaries(String userId) {
        return jdbc.sql("""
                SELECT l.id, l.title, l.description,
                       (SELECT count(*) FROM activities a WHERE a.lesson_id = l.id) AS total,
                       p.current_position, p.status
                FROM lessons l
                LEFT JOIN user_progress p ON p.lesson_id = l.id AND p.user_id = :userId
                WHERE l.published
                ORDER BY l.position
                """)
                .param("userId", userId)
                .query((rs, i) -> {
                    int total = rs.getInt("total");
                    int position = rs.getInt("current_position");
                    boolean started = !rs.wasNull();
                    ProgressStatus status = started
                            ? ProgressStatus.valueOf(rs.getString("status"))
                            : ProgressStatus.NOT_STARTED;
                    return new LessonSummary(rs.getString("id"), rs.getString("title"),
                            rs.getString("description"), total, Math.min(position, total), status);
                })
                .list();
    }

    public Optional<LessonInfo> findLesson(String lessonId) {
        return jdbc.sql("SELECT id, title, description FROM lessons WHERE id = :id AND published")
                .param("id", lessonId)
                .query((rs, i) -> new LessonInfo(rs.getString("id"), rs.getString("title"),
                        rs.getString("description")))
                .optional();
    }

    public int countActivities(String lessonId) {
        return jdbc.sql("SELECT count(*) FROM activities WHERE lesson_id = :id")
                .param("id", lessonId)
                .query(Integer.class)
                .single();
    }

    public List<String> findActivityTitles(String lessonId) {
        return jdbc.sql("SELECT title FROM activities WHERE lesson_id = :id ORDER BY position")
                .param("id", lessonId)
                .query(String.class)
                .list();
    }

    public Optional<Activity> findActivityAt(String lessonId, int position) {
        return jdbc.sql("""
                SELECT id, position, type, title, instruction,
                       CAST(content AS text) AS content,
                       answer, praise_message, clue_message, explanation
                FROM activities
                WHERE lesson_id = :lessonId AND position = :position
                """)
                .param("lessonId", lessonId)
                .param("position", position)
                .query(LessonRepository::mapActivity)
                .optional();
    }

    public Optional<Progress> findProgress(String userId, String lessonId) {
        return jdbc.sql("""
                SELECT current_position, status FROM user_progress
                WHERE user_id = :userId AND lesson_id = :lessonId
                """)
                .param("userId", userId)
                .param("lessonId", lessonId)
                .query((rs, i) -> new Progress(rs.getInt("current_position"),
                        ProgressStatus.valueOf(rs.getString("status"))))
                .optional();
    }

    public void createProgressIfAbsent(String userId, String lessonId) {
        jdbc.sql("""
                INSERT INTO user_progress (user_id, lesson_id) VALUES (:userId, :lessonId)
                ON CONFLICT (user_id, lesson_id) DO NOTHING
                """)
                .param("userId", userId)
                .param("lessonId", lessonId)
                .update();
    }

    /** Moves progress forward only if it is still where we expect (guards against double submits). */
    public boolean advance(String userId, String lessonId, int expectedPosition, int newPosition,
            ProgressStatus newStatus) {
        int rows = jdbc.sql("""
                UPDATE user_progress
                SET current_position = :newPosition, status = :status, updated_at = now()
                WHERE user_id = :userId AND lesson_id = :lessonId
                  AND current_position = :expected AND status = 'IN_PROGRESS'
                """)
                .param("newPosition", newPosition)
                .param("status", newStatus.name())
                .param("userId", userId)
                .param("lessonId", lessonId)
                .param("expected", expectedPosition)
                .update();
        return rows == 1;
    }

    public void resetProgress(String userId, String lessonId) {
        jdbc.sql("""
                UPDATE user_progress
                SET current_position = 0, status = 'IN_PROGRESS', updated_at = now()
                WHERE user_id = :userId AND lesson_id = :lessonId
                """)
                .param("userId", userId)
                .param("lessonId", lessonId)
                .update();
    }

    private static Activity mapActivity(ResultSet rs, int rowNum) throws SQLException {
        Array sqlArray = rs.getArray("answer");
        List<String> answer = sqlArray == null ? null : List.of((String[]) sqlArray.getArray());
        return new Activity(
                rs.getLong("id"),
                rs.getInt("position"),
                ActivityType.valueOf(rs.getString("type")),
                rs.getString("title"),
                rs.getString("instruction"),
                rs.getString("content"),
                answer,
                rs.getString("praise_message"),
                rs.getString("clue_message"),
                rs.getString("explanation"));
    }
}
