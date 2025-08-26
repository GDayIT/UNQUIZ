/**
 * BusinesslogicaDelegation defines the delegation boundary between GUI and
 * business logic. GUI calls these methods (possibly via lambdas) instead of
 * directly wiring UI components to the controller.
 */
package dbbl;

import java.util.List;

public interface BusinesslogicaDelegation {
    // Theme operations
    List<String> getAllTopics();
    void saveTheme(String title, String description);
    void deleteTheme(String title);
    String getThemeDescription(String title);

    // Question operations
    List<String> getQuestionTitles(String topic);
    RepoQuizeeQuestions getQuestion(String topic, int index);
    void saveQuestion(String topic, String title, String text, List<String> answers, List<Boolean> correct);
    void saveQuestion(String topic, String title, String text, String explanation, List<String> answers, List<Boolean> correct);
    void deleteQuestion(String topic, int index);

    // Persistence lifecycle
    default void saveAll() {}
}
