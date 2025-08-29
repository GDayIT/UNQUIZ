package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.ArrayList;
import java.util.List;

/**
 * QuizDataMapper is a **utility class** for converting between different quiz data models.
 * <p>
 * It provides mapping methods between:
 * <ul>
 *     <li>{@link RepoQuizeeQuestions} - business layer representation</li>
 *     <li>{@link QuizQuestion} - GUI layer representation</li>
 *     <li>{@link QuizFormData} - form data transfer object for editing/creating questions</li>
 * </ul>
 * <p>
 * By centralizing conversions, the class ensures **consistent data handling** across
 * Themes, Questions, Leitner Cards, Quiz Sessions, and persistent storage.
 * <p>
 * All methods are static and **null-safe**, returning null or empty lists when inputs are null.
 * Instantiation is prevented.
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class QuizDataMapper {
    
    /**
     * Private constructor to prevent instantiation.
     * This class is a static utility for model conversion.
     */
    private QuizDataMapper() {
        // Utility class - no instances needed
    }
    
    /**
     * Converts a {@link RepoQuizeeQuestions} to a {@link QuizQuestion}.
     * Maps fields: title, text, answers, correctness flags, theme, and creation timestamp.
     *
     * @param repo the repository question to convert
     * @return the corresponding QuizQuestion, or null if repo is null
     */
    public static QuizQuestion toQuizQuestion(RepoQuizeeQuestions repo) {
        if (repo == null) return null;
        
        List<String> antworten = repo.getAntworten();
        List<Boolean> korrekt = repo.getKorrekt();
        
        String[] antwortenArray = antworten.toArray(new String[0]);
        boolean[] korrektArray = new boolean[korrekt.size()];
        for (int i = 0; i < korrekt.size(); i++) {
            korrektArray[i] = Boolean.TRUE.equals(korrekt.get(i));
        }
        
        QuizQuestion question = new QuizQuestion(
            repo.getTitel(),
            repo.getFrageText(),
            antwortenArray,
            korrektArray
        );
        question.setThema(repo.getThema());
        question.setCreatedAt(repo.getCreatedAt());
        
        return question;
    }
    
    /**
     * Converts a {@link QuizQuestion} to {@link RepoQuizeeQuestions}.
     * Maps GUI fields back to the business layer representation.
     *
     * @param question the quiz question to convert
     * @return the corresponding repository question, or null if question is null
     */
    public static RepoQuizeeQuestions toRepoQuestion(QuizQuestion question) {
        if (question == null) return null;
        
        List<String> antworten = question.getAntworten();
        List<Boolean> korrekt = question.getKorrekt();
        
        String[] antwortenArray = antworten.toArray(new String[0]);
        boolean[] korrektArray = new boolean[korrekt.size()];
        for (int i = 0; i < korrekt.size(); i++) {
            korrektArray[i] = Boolean.TRUE.equals(korrekt.get(i));
        }
        
        RepoQuizeeQuestions repo = new RepoQuizeeQuestions(
            question.getTitel(),
            question.getFrageText(),
            antwortenArray,
            korrektArray
        );
        repo.setThema(question.getThema());
        
        return repo;
    }
    
    /**
     * Converts {@link QuizFormData} to {@link RepoQuizeeQuestions}.
     * Useful for persisting form input directly to business layer.
     *
     * @param formData the form data to convert
     * @return the corresponding repository question, or null if formData is null
     */
    public static RepoQuizeeQuestions toRepoQuestion(QuizFormData formData) {
        if (formData == null) return null;
        
        return new RepoQuizeeQuestions(
            formData.getTitel(),
            formData.getFrage(),
            formData.getAntworten(),
            formData.getKorrekt()
        );
    }
    
    /**
     * Converts a {@link RepoQuizeeQuestions} to {@link QuizFormData}.
     * Useful for populating edit forms from persisted questions.
     *
     * @param repo the repository question to convert
     * @return the corresponding form data, or null if repo is null
     */
    public static QuizFormData toFormData(RepoQuizeeQuestions repo) {
        if (repo == null) return null;
        
        List<String> antworten = repo.getAntworten();
        List<Boolean> korrekt = repo.getKorrekt();
        
        String[] antwortenArray = antworten.toArray(new String[0]);
        boolean[] korrektArray = new boolean[korrekt.size()];
        for (int i = 0; i < korrekt.size(); i++) {
            korrektArray[i] = Boolean.TRUE.equals(korrekt.get(i));
        }
        
        return new QuizFormData(
            repo.getTitel(),
            repo.getFrageText(),
            antwortenArray,
            korrektArray
        );
    }
    
    /**
     * Converts a {@link QuizQuestion} to {@link QuizFormData}.
     * Supports GUI form population.
     *
     * @param question the quiz question to convert
     * @return the corresponding form data, or null if question is null
     */
    public static QuizFormData toFormData(QuizQuestion question) {
        if (question == null) return null;
        
        List<String> antworten = question.getAntworten();
        List<Boolean> korrekt = question.getKorrekt();
        
        String[] antwortenArray = antworten.toArray(new String[0]);
        boolean[] korrektArray = new boolean[korrekt.size()];
        for (int i = 0; i < korrekt.size(); i++) {
            korrektArray[i] = Boolean.TRUE.equals(korrekt.get(i));
        }
        
        return new QuizFormData(
            question.getTitel(),
            question.getFrageText(),
            antwortenArray,
            korrektArray
        );
    }
    
    /**
     * Converts a list of {@link RepoQuizeeQuestions} to a list of {@link QuizQuestion}.
     * 
     * @param repoList the repository questions to convert
     * @return the converted list of QuizQuestions
     */
    public static List<QuizQuestion> toQuizQuestionList(List<RepoQuizeeQuestions> repoList) {
        if (repoList == null) return new ArrayList<>();
        
        List<QuizQuestion> result = new ArrayList<>();
        for (RepoQuizeeQuestions repo : repoList) {
            QuizQuestion question = toQuizQuestion(repo);
            if (question != null) {
                result.add(question);
            }
        }
        return result;
    }
    
    /**
     * Converts a list of {@link QuizQuestion} to a list of {@link RepoQuizeeQuestions}.
     * 
     * @param questionList the quiz questions to convert
     * @return the converted list of repository questions
     */
    public static List<RepoQuizeeQuestions> toRepoQuestionList(List<QuizQuestion> questionList) {
        if (questionList == null) return new ArrayList<>();
        
        List<RepoQuizeeQuestions> result = new ArrayList<>();
        for (QuizQuestion question : questionList) {
            RepoQuizeeQuestions repo = toRepoQuestion(question);
            if (repo != null) {
                result.add(repo);
            }
        }
        return result;
    }
}