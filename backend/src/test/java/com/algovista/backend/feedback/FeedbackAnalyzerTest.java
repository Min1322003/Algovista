package com.algovista.backend.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import com.algovista.backend.lesson.Activity;
import com.algovista.backend.lesson.ActivityType;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeedbackAnalyzerTest {

    private final FeedbackAnalyzer analyzer = new FeedbackAnalyzer();

    private Activity activity(ActivityType type, List<String> answer) {
        return new Activity(1, 0, type, "title", "instruction", "{}", answer, "Great!", "Try again", "Because.");
    }

    @Test
    void explanationAlwaysPasses() {
        assertThat(analyzer.analyzeAnswer(activity(ActivityType.EXPLANATION, null), List.of()).correct()).isTrue();
    }

    @Test
    void exploreActivityCompletesWithoutAnAnswer() {
        assertThat(analyzer.analyzeAnswer(activity(ActivityType.EXPLORE, null), List.of()).correct()).isTrue();
    }

    @Test
    void multipleChoiceChecksTheSelectedId() {
        Activity a = activity(ActivityType.MULTIPLE_CHOICE, List.of("A"));
        assertThat(analyzer.analyzeAnswer(a, List.of("A")).correct()).isTrue();
        assertThat(analyzer.analyzeAnswer(a, List.of("B")).correct()).isFalse();
        assertThat(analyzer.analyzeAnswer(a, List.of()).correct()).isFalse();
    }

    @Test
    void edgeSelectionIgnoresOrderButNotMissingOrExtraItems() {
        Activity a = activity(ActivityType.EDGE_SELECTION, List.of("AC", "BC"));
        assertThat(analyzer.analyzeAnswer(a, List.of("BC", "AC")).correct()).isTrue();
        assertThat(analyzer.analyzeAnswer(a, List.of("AC")).correct()).isFalse();
        assertThat(analyzer.analyzeAnswer(a, List.of("AC", "BC", "AB")).correct()).isFalse();
    }

    @Test
    void codeCompletionRequiresTheSameOrder() {
        Activity a = activity(ActivityType.CODE_COMPLETION, List.of("t1", "t2", "t3"));
        assertThat(analyzer.analyzeAnswer(a, List.of("t1", "t2", "t3")).correct()).isTrue();
        assertThat(analyzer.analyzeAnswer(a, List.of("t3", "t2", "t1")).correct()).isFalse();
    }

    @Test
    void correctAnswerGetsPraiseAndExplanationButNoClue() {
        Feedback f = analyzer.analyzeAnswer(activity(ActivityType.MULTIPLE_CHOICE, List.of("A")), List.of("A"))
                .feedback();
        assertThat(f.praiseMessage()).isEqualTo("Great!");
        assertThat(f.explanation()).isEqualTo("Because.");
        assertThat(f.clueMessage()).isNull();
    }

    @Test
    void wrongAnswerGetsClueButNoExplanation() {
        Feedback f = analyzer.analyzeAnswer(activity(ActivityType.MULTIPLE_CHOICE, List.of("A")), List.of("B"))
                .feedback();
        assertThat(f.clueMessage()).isEqualTo("Try again");
        assertThat(f.praiseMessage()).isNull();
        assertThat(f.explanation()).isNull();
    }
}
