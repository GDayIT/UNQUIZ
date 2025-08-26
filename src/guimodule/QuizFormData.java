package guimodule;

/**
 * Simple DTO to transfer form data from UI without binding directly to RepoQuizeeQuestions.
 */
public class QuizFormData {
    private final String titel;
    private final String frage;
    private final String[] antworten;
    private final boolean[] korrekt;

    public QuizFormData(String titel, String frage, String[] antworten, boolean[] korrekt) {
        this.titel = titel;
        this.frage = frage;
        this.antworten = antworten;
        this.korrekt = korrekt;
    }

    public String getTitel() { return titel; }
    public String getFrage() { return frage; }
    public String[] getAntworten() { return antworten; }
    public boolean[] getKorrekt() { return korrekt; }
    
    
}