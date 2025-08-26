package guimodule;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Immutable-like model for a quiz question with multiple answers.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class QuizQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titel;
    private String frageText;
    private String erklaerung;
    private List<String> antworten = new ArrayList<>();
    private List<Boolean> korrekt = new ArrayList<>();
    private String thema;
    private LocalDateTime createdAt = LocalDateTime.now();

    public QuizQuestion() {}

    public QuizQuestion(String titel, String frageText, String[] antworten, boolean[] korrekt) {
        this.titel = titel != null ? titel : "";
        this.frageText = frageText != null ? frageText : "";
        if (antworten != null) this.antworten = new ArrayList<>(Arrays.asList(antworten));
        if (korrekt != null) {
            this.korrekt = new ArrayList<>(korrekt.length);
            for (boolean b : korrekt) this.korrekt.add(b);
        }
    }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getFrageText() { return frageText; }
    public void setFrageText(String frageText) { this.frageText = frageText; }

    public String getErklaerung() { return erklaerung; }
    public void setErklaerung(String erklaerung) { this.erklaerung = erklaerung; }

    public List<String> getAntworten() { return new ArrayList<>(antworten); }
    public void setAntworten(List<String> antworten) { this.antworten = new ArrayList<>(antworten); }

    public List<Boolean> getKorrekt() { return new ArrayList<>(korrekt); }
    public void setKorrekt(List<Boolean> korrekt) { this.korrekt = new ArrayList<>(korrekt); }

    public String getThema() { return thema; }
    public void setThema(String thema) { this.thema = thema; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
