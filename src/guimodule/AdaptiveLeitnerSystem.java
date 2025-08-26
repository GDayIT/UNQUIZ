package guimodule;

import dbbl.BusinesslogicaDelegation;
import dbbl.RepoQuizeeQuestions;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptive Leitner System Manager.
 *
 * Verwaltet den Lernfortschritt mittels eines erweiterten Leitner-Kastensystems
 * (Spaced Repetition) und integriert sich in die modulare Quiz-Anwendung. Das
 * System speichert seinen Zustand (Karten, Levels, Fälligkeit, Statistiken) persistent
 * in einer separaten Datei, unabhängig von der Datenhaltung der eigentlichen Fragen.
 *
 * Kernaspekte:
 * - Persistenter Zustand des Leitner-Systems (keine Test-/Beispieldaten)
 * - Delegation zur Business-Logik, um Fragen/Themen abzurufen
 * - Bereitstellung fälliger Fragen pro Thema oder global
 * - Statistiken pro Level und Thema
 *
 * Serialisierung:
 * - Der Delegat (BusinesslogicaDelegation) wird nicht serialisiert (transient).
 * - Beim Speichern wird der Zeitstempel lastSystemUpdate aktualisiert.
 * - Beim Laden werden nur die internen Daten (Karten, Statistiken) übernommen,
 *   die Laufzeitabhängigkeiten bleiben unverändert.
 */
public class AdaptiveLeitnerSystem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final String LEITNER_DATA_FILE = "leitner_system.dat";
    
    // Karten-Verwaltung
    private final Map<String, AdaptiveLeitnerCard> cards = new ConcurrentHashMap<>();
    private final transient BusinesslogicaDelegation delegate;
    
    // System-Statistiken
    private int totalReviews = 0;
    private LocalDate lastSystemUpdate = LocalDate.now();

    /**
     * Serializable snapshot that contains only the minimal state required for
     * persistence. This prevents accidental serialization of runtime-only
     * dependencies and stabilizes the file format across versions.
     */
    private static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, AdaptiveLeitnerCard> cards;
        int totalReviews;
        LocalDate lastSystemUpdate;
    }
    
    public AdaptiveLeitnerSystem(BusinesslogicaDelegation delegate) {
        this.delegate = delegate;
        loadSystem();
        // Hinweis: Keine automatische Initialisierung neuer Karten mehr.
        // Karten werden nur durch echte Quiz-Ergebnisse (processQuizResult) angelegt.
    }

    /**
     * Merge policy for combining incoming Leitner cards with existing state.
     */
    public enum MergePolicy {
        /** Keep existing cards when conflict occurs. */
        PREFER_EXISTING,
        /** Replace existing cards with incoming. */
        PREFER_INCOMING,
        /** Choose the card with the higher Leitner level. */
        PREFER_HIGHER_LEVEL,
        /** Choose the card with the newer lastReviewed timestamp. */
        PREFER_NEWER
    }

    /**
     * Merges incoming cards with the current Leitner state according to the given policy.
     * Incoming map is identified by questionId (theme:title). Persisted afterwards.
     *
     * @param incoming map of cards to merge
     * @param policy merge policy
     */
    public void mergeCards(Map<String, AdaptiveLeitnerCard> incoming, MergePolicy policy) {
        if (incoming == null || incoming.isEmpty()) return;
        for (Map.Entry<String, AdaptiveLeitnerCard> e : incoming.entrySet()) {
            String id = e.getKey();
            AdaptiveLeitnerCard inc = e.getValue();
            AdaptiveLeitnerCard cur = this.cards.get(id);
            if (cur == null) {
                this.cards.put(id, inc);
                continue;
            }
            AdaptiveLeitnerCard chosen = cur;
            switch (policy) {
                case PREFER_EXISTING: {
                    chosen = cur; break;
                }
                case PREFER_INCOMING: {
                    chosen = inc; break;
                }
                case PREFER_HIGHER_LEVEL: {
                    int lCur = cur.getLevel();
                    int lInc = inc.getLevel();
                    if (lInc > lCur) chosen = inc; else if (lInc < lCur) chosen = cur; else {
                        // tie-breaker by lastReviewed
                        chosen = pickNewer(cur, inc);
                    }
                    break;
                }
                case PREFER_NEWER: {
                    chosen = pickNewer(cur, inc);
                    break;
                }
                default: {
                    chosen = cur; break;
                }
            }
            this.cards.put(id, chosen);
        }
        saveSystem();
    }

    private AdaptiveLeitnerCard pickNewer(AdaptiveLeitnerCard a, AdaptiveLeitnerCard b) {
        java.time.LocalDateTime ra = a.getLastReviewed();
        java.time.LocalDateTime rb = b.getLastReviewed();
        if (ra == null && rb == null) return a;
        if (ra == null) return b;
        if (rb == null) return a;
        return rb.isAfter(ra) ? b : a;
    }
    
    /**
     * Initialisiert neue Fragen, die noch keine Leitner-Karten haben
     */
    private void initializeNewQuestions() {
        if (delegate == null) return;

        try {
            List<String> themes = delegate.getAllTopics();

            for (String theme : themes) {
                List<String> questionTitles = delegate.getQuestionTitles(theme);

                for (int i = 0; i < questionTitles.size(); i++) {
                    RepoQuizeeQuestions question = delegate.getQuestion(theme, i);
                    if (question != null) {
                        String questionId = generateQuestionId(question);

                        if (!cards.containsKey(questionId)) {
                            AdaptiveLeitnerCard card = new AdaptiveLeitnerCard(
                                questionId,
                                question.getThema(),
                                question.getTitel()
                            );
                            cards.put(questionId, card);
                        }
                    }
                }
            }

            saveSystem();
        } catch (Exception e) {
            System.err.println("Error initializing Leitner cards: " + e.getMessage());
        }
    }
    
    /**
     * Verarbeitet ein Quiz-Ergebnis und aktualisiert die entsprechende Karte
     */
    /**
     * Verarbeitet das Ergebnis einer beantworteten Frage und aktualisiert die
     * entsprechende Leitner-Karte. Legt bei Bedarf eine neue Karte an.
     *
     * Thread-Sicherheit: Diese Methode ist nicht synchronisiert. Wenn sie aus
     * mehreren Threads aufgerufen wird, sollten Aufrufe serialisiert oder eine
     * externe Synchronisation verwendet werden.
     *
     * Persistenz: Nach der Aktualisierung wird der Systemzustand gespeichert.
     *
     * @param result Ergebnisobjekt des Quiz-Spiels (Thema, Titel, Korrektheit, Antwortzeit)
     */
    public void processQuizResult(ModularQuizPlay.QuizResult result) {
        String questionId = generateQuestionId(result.theme, result.questionTitle);
        
        AdaptiveLeitnerCard card = cards.get(questionId);
        if (card == null) {
            // Neue Karte erstellen falls nicht vorhanden
            card = new AdaptiveLeitnerCard(questionId, result.theme, result.questionTitle);
            cards.put(questionId, card);
        }
        
        // Ergebnis verarbeiten
        card.processResult(result.isCorrect, result.getAnswerTimeSeconds());
        totalReviews++;
        
        saveSystem();
    }
    
    /**
     * Gibt fällige Fragen für ein bestimmtes Thema zurück
     */
    /**
     * Ermittelt alle fälligen Fragen für ein gegebenes Thema und sortiert sie
     * absteigend nach Priorität (wichtigste zuerst).
     *
     * @param theme Das gewünschte Thema
     * @return Liste fälliger Fragen des Themas (leer, wenn keine fällig sind)
     */
    public List<RepoQuizeeQuestions> getDueQuestions(String theme) {
        if (delegate == null) return new ArrayList<>();

        try {
            List<String> questionTitles = delegate.getQuestionTitles(theme);
            List<RepoQuizeeQuestions> dueQuestions = new ArrayList<>();

            for (int i = 0; i < questionTitles.size(); i++) {
                RepoQuizeeQuestions question = delegate.getQuestion(theme, i);
                if (question != null) {
                    String questionId = generateQuestionId(question);
                    AdaptiveLeitnerCard card = cards.get(questionId);

                    if (card != null && card.isDue()) {
                        dueQuestions.add(question);
                    }
                }
            }

            // Sortiere nach Priorität (wichtigste zuerst)
            dueQuestions.sort((q1, q2) -> {
                String id1 = generateQuestionId(q1);
                String id2 = generateQuestionId(q2);
                AdaptiveLeitnerCard card1 = cards.get(id1);
                AdaptiveLeitnerCard card2 = cards.get(id2);

                if (card1 == null || card2 == null) return 0;
                return Double.compare(card2.getPriority(), card1.getPriority());
            });

            return dueQuestions;

        } catch (Exception e) {
            System.err.println("Error getting due questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Gibt alle fälligen Fragen zurück (themenübergreifend)
     */
    /**
     * Ermittelt alle fälligen Fragen über alle Themen hinweg und sortiert sie
     * absteigend nach Priorität (wichtigste zuerst).
     *
     * @return Liste aller fälligen Fragen (leer, wenn keine fällig sind)
     */
    public List<RepoQuizeeQuestions> getAllDueQuestions() {
        if (delegate == null) return new ArrayList<>();

        try {
            List<RepoQuizeeQuestions> allDueQuestions = new ArrayList<>();
            List<String> themes = delegate.getAllTopics();

            for (String theme : themes) {
                List<RepoQuizeeQuestions> themeDueQuestions = getDueQuestions(theme);
                allDueQuestions.addAll(themeDueQuestions);
            }

            // Sortiere nach Priorität (wichtigste zuerst)
            allDueQuestions.sort((q1, q2) -> {
                String id1 = generateQuestionId(q1);
                String id2 = generateQuestionId(q2);
                AdaptiveLeitnerCard card1 = cards.get(id1);
                AdaptiveLeitnerCard card2 = cards.get(id2);

                if (card1 == null || card2 == null) return 0;
                return Double.compare(card2.getPriority(), card1.getPriority());
            });

            return allDueQuestions;

        } catch (Exception e) {
            System.err.println("Error getting all due questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Gibt Statistiken für ein bestimmtes Thema zurück
     */
    /**
     * Liefert eine Aufteilung der Karten nach Leitner-Leveln für ein bestimmtes Thema.
     * Wird null oder "Alle Themen" übergeben, werden alle Karten berücksichtigt.
     *
     * @param theme Thema oder null/"Alle Themen" für globale Sicht
     * @return Map mit Level -> Liste Karten
     */
    public Map<Integer, List<AdaptiveLeitnerCard>> getThemeStatistics(String theme) {
        Map<Integer, List<AdaptiveLeitnerCard>> levelStats = new HashMap<>();
        
        // Initialisiere alle Level
        for (int level = 1; level <= 6; level++) {
            levelStats.put(level, new ArrayList<>());
        }
        
        // Filtere Karten nach Thema
        cards.values().stream()
            .filter(card -> theme == null || "Alle Themen".equals(theme) || theme.equals(card.getTheme()))
            .forEach(card -> levelStats.get(card.getLevel()).add(card));
        
        return levelStats;
    }
    
    /**
     * Gibt Gesamtstatistiken zurück
     */
    /**
     * Liefert Gesamtstatistiken (alle Themen) als Level-zu-Karten-Zuordnung.
     *
     * @return Map mit Level -> Liste Karten
     */
    public Map<Integer, List<AdaptiveLeitnerCard>> getAllStatistics() {
        return getThemeStatistics(null);
    }
    
    /**
     * Gibt die Anzahl fälliger Fragen pro Level zurück
     */
    /**
     * Zählt die Anzahl fälliger Karten pro Level für das angegebene Thema oder global,
     * wenn null/"Alle Themen" übergeben wird.
     *
     * @param theme Thema oder null/"Alle Themen"
     * @return Map Level -> Anzahl fälliger Karten
     */
    public Map<Integer, Integer> getDueCountByLevel(String theme) {
        Map<Integer, Integer> dueCounts = new HashMap<>();
        
        for (int level = 1; level <= 6; level++) {
            dueCounts.put(level, 0);
        }
        
        cards.values().stream()
            .filter(card -> theme == null || "Alle Themen".equals(theme) || theme.equals(card.getTheme()))
            .filter(AdaptiveLeitnerCard::isDue)
            .forEach(card -> dueCounts.merge(card.getLevel(), 1, Integer::sum));
        
        return dueCounts;
    }
    
    /**
     * Gibt Karte für eine bestimmte Frage zurück
     */
    /**
     * Gibt die Karte zu einer bestimmten Frage zurück.
     *
     * @param theme Thema
     * @param questionTitle Fragetitel
     * @return Karte oder null, wenn keine vorhanden ist
     */
    public AdaptiveLeitnerCard getCard(String theme, String questionTitle) {
        String questionId = generateQuestionId(theme, questionTitle);
        return cards.get(questionId);
    }
    
    /**
     * Gibt alle Karten für ein Thema zurück
     */
    /**
     * Gibt alle Karten für ein bestimmtes Thema zurück. Wenn null oder
     * "Alle Themen" übergeben wird, werden alle Karten zurückgegeben.
     *
     * @param theme Thema oder null/"Alle Themen"
     * @return sortierte Liste der Karten (nach Fragetitel)
     */
    public List<AdaptiveLeitnerCard> getCardsForTheme(String theme) {
        return cards.values().stream()
            .filter(card -> theme == null || "Alle Themen".equals(theme) || theme.equals(card.getTheme()))
            .sorted((a, b) -> a.getQuestionTitle().compareTo(b.getQuestionTitle()))
            .collect(Collectors.toList());
    }
    
    /**
     * Generiert eindeutige ID für eine Frage
     */
    /**
     * Erzeugt eine stabile, eindeutige ID für eine Frage auf Basis von Thema und Titel.
     *
     * @param question Frageobjekt
     * @return ID in der Form "<thema>:<titel>"
     */
    private String generateQuestionId(RepoQuizeeQuestions question) {
        return generateQuestionId(question.getThema(), question.getTitel());
    }
    
    /**
     * Erzeugt eine stabile, eindeutige ID für eine Frage auf Basis von Thema und Titel.
     *
     * @param theme Thema
     * @param title Titel der Frage
     * @return ID in der Form "<thema>:<titel>"
     */
    private String generateQuestionId(String theme, String title) {
        return theme + ":" + title;
    }
    
    /**
     * Speichert das Leitner-System
     */
    /**
     * Speichert den aktuellen Zustand des Leitner-Systems in eine Datei.
     * Aktualisiert vor dem Speichern den Zeitstempel lastSystemUpdate.
     */
    private void saveSystem() {
        this.lastSystemUpdate = LocalDate.now();
        File target = new File(LEITNER_DATA_FILE);
        File tmp = new File(LEITNER_DATA_FILE + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp))) {
            Snapshot snapshot = new Snapshot();
            snapshot.cards = new HashMap<>(this.cards);
            snapshot.totalReviews = this.totalReviews;
            snapshot.lastSystemUpdate = this.lastSystemUpdate;
            oos.writeObject(snapshot);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving Leitner system: " + e.getMessage());
            if (tmp.exists()) tmp.delete();
            return;
        }
        // Atomic replace
        if (target.exists() && !target.delete()) {
            System.err.println("Error saving Leitner system: unable to replace existing file");
            tmp.delete();
            return;
        }
        if (!tmp.renameTo(target)) {
            System.err.println("Error saving Leitner system: unable to finalize save");
            tmp.delete();
        }
    }
    
    /**
     * Lädt das Leitner-System
     */
    /**
     * Lädt den Zustand des Leitner-Systems aus der Datei, sofern vorhanden.
     * Bei Fehlern wird mit einem leeren System fortgesetzt.
     */
    private void loadSystem() {
        File file = new File(LEITNER_DATA_FILE);
        if (!file.exists()) {
            return; // Neues System
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Snapshot) {
                Snapshot snap = (Snapshot) obj;
                this.cards.clear();
                this.cards.putAll(snap.cards != null ? snap.cards : Collections.emptyMap());
                this.totalReviews = snap.totalReviews;
                this.lastSystemUpdate = snap.lastSystemUpdate != null ? snap.lastSystemUpdate : LocalDate.now();
            } else if (obj instanceof AdaptiveLeitnerSystem) {
                AdaptiveLeitnerSystem loaded = (AdaptiveLeitnerSystem) obj;
                // Legacy fallback: copy state fields only
                this.cards.clear();
                this.cards.putAll(loaded.cards);
                this.totalReviews = loaded.totalReviews;
                this.lastSystemUpdate = loaded.lastSystemUpdate;
            } else {
                // Unrecognized format: start fresh
                throw new IOException("Unrecognized Leitner data format: " + obj.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading Leitner system: " + e.getMessage());
            // Datei könnte korrupt sein (z. B. abgebrochene Schreibvorgänge) → löschen und sauber starten
            if (file.exists() && !file.delete()) {
                System.err.println("Unable to remove corrupt Leitner data file");
            }
        }
    }
    
    /**
     * Setzt das gesamte System zurück
     */
    /**
     * Setzt das Leitner-System vollständig zurück (löscht alle Karten und Statistiken),
     * entfernt die Persistenzdatei und initialisiert Karten für vorhandene Fragen neu.
     */
    public void resetSystem() {
        cards.clear();
        totalReviews = 0;
        lastSystemUpdate = LocalDate.now();
        
        // Datei löschen
        File file = new File(LEITNER_DATA_FILE);
        if (file.exists()) {
            file.delete();
        }
        // Keine automatische Neu-Initialisierung mehr
    }
    
    // ================ GETTERS ================
    
    public int getTotalCards() { return cards.size(); }
    public int getTotalReviews() { return totalReviews; }
    public LocalDate getLastSystemUpdate() { return lastSystemUpdate; }
    
    public int getDueCardsCount() {
        return (int) cards.values().stream().filter(AdaptiveLeitnerCard::isDue).count();
    }
    
    public int getDueCardsCount(String theme) {
        return (int) cards.values().stream()
            .filter(card -> theme == null || "Alle Themen".equals(theme) || theme.equals(card.getTheme()))
            .filter(AdaptiveLeitnerCard::isDue)
            .count();
    }
}
