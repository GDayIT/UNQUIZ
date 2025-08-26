package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mapping between different quiz data models.
 * 
 * This class provides conversion methods between:
 * - RepoQuizeeQuestions (business layer model)
 * - QuizQuestion (GUI layer model)
 * - QuizFormData (form data transfer object)
 * 
 * This ensures consistent data handling across different layers of the application.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class QuizDataMapper {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private QuizDataMapper() {
        // Utility class - no instances needed
    }
    
    /**
     * Converts a RepoQuizeeQuestions to a QuizQuestion.
     * @param repo the repository question to convert
     * @return the converted quiz question, or null if input is null
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
     * Converts a QuizQuestion to a RepoQuizeeQuestions.
     * @param question the quiz question to convert
     * @return the converted repository question, or null if input is null
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
     * Converts QuizFormData to a RepoQuizeeQuestions.
     * @param formData the form data to convert
     * @return the converted repository question, or null if input is null
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
     * Converts a RepoQuizeeQuestions to QuizFormData.
     * @param repo the repository question to convert
     * @return the converted form data, or null if input is null
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
     * Converts a QuizQuestion to QuizFormData.
     * @param question the quiz question to convert
     * @return the converted form data, or null if input is null
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
     * Converts a list of RepoQuizeeQuestions to a list of QuizQuestions.
     * @param repoList the list of repository questions to convert
     * @return the converted list of quiz questions
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
     * Converts a list of QuizQuestions to a list of RepoQuizeeQuestions.
     * @param questionList the list of quiz questions to convert
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
