package com.algovista.backend.feedback;

import com.algovista.backend.lesson.Activity;
import com.algovista.backend.lesson.ActivityType;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

/** Simple stand-in: compares submitted ids with the stored answer ids. Real rules come in Part 6. */
@Component
public class FeedbackAnalyzer {

    public FeedbackResult analyzeAnswer(Activity activity, List<String> answers) {
        boolean correct = determineCorrectness(activity, answers);
        return new FeedbackResult(correct, generateFeedback(activity, correct));
    }

    public boolean determineCorrectness(Activity activity, List<String> answers) {
        if (activity.type() == ActivityType.EXPLANATION || activity.type() == ActivityType.EXPLORE) {
            return true; // nothing to answer, "Continue" always succeeds
        }
        List<String> expected = activity.answer();
        if (expected == null || expected.isEmpty()) {
            throw new IllegalStateException("Activity " + activity.id() + " has no answer configured");
        }
        if (activity.type() == ActivityType.CODE_COMPLETION) {
            return expected.equals(answers); // order matters
        }
        return expected.size() == answers.size()
                && new HashSet<>(expected).equals(new HashSet<>(answers)); // order does not matter
    }

    public Feedback generateFeedback(Activity activity, boolean correct) {
        if (correct) {
            return new Feedback(activity.praiseMessage(), null, activity.explanation());
        }
        return new Feedback(null, activity.clueMessage(), null); // explanation could reveal the answer
    }
}
