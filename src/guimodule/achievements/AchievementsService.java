package guimodule.achievements;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service responsible for managing user achievements.
 * <p>
 * Achievements are independent from questions, Leitner system, and statistics.
 * They can be updated via events triggered by other modules (questions answered,
 * Leitner reviews, themes created, etc.).
 * <p>
 * This class provides:
 * <ul>
 *   <li>Unlocking achievements</li>
 *   <li>Querying unlocked achievements</li>
 *   <li>Persistence to file</li>
 *   <li>Merging external snapshots</li>
 * </ul>
 * <p>
 * The achievements system is serializable for persistence.
 * 
 * @author D.Georgiou
 * @version 1.0
 */
public class AchievementsService implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Default file used to persist achievements */
    private static final String FILE = "achievements.dat";

    // === ENUM: Defined achievements ===
    /**
     * Enum representing all possible achievements that can be unlocked.
     */
    public enum Achievement {
        FIRST_CORRECT_ANSWER,
        TEN_CORRECT_IN_A_ROW,
        FIRST_THEME_CREATED,
        TEN_QUESTIONS_CREATED,
        FIRST_LEITNER_REVIEW,
        LEITNER_LEVEL6_ACHIEVED
    }

    // === ENTRY CLASS ===
    /**
     * Represents a single unlocked achievement with timestamp.
     */
    public static class Entry implements Serializable {
        private static final long serialVersionUID = 1L;

        /** The achievement unlocked */
        public final Achievement achievement;

        /** The timestamp when the achievement was unlocked */
        public final LocalDateTime unlockedAt;

        /**
         * Constructor for creating an achievement entry.
         * 
         * @param a Achievement unlocked
         * @param t Time it was unlocked
         */
        public Entry(Achievement a, LocalDateTime t) {
            this.achievement = a;
            this.unlockedAt = t;
        }
    }

    // === SNAPSHOT CLASS ===
    /**
     * Serializable snapshot containing a map of unlocked achievements and their timestamps.
     * Used for persistence and merging achievements across sessions.
     */
    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Map of achievements to their unlock timestamps */
        public Map<Achievement, LocalDateTime> unlocked = new HashMap<>();
    }

    // === INTERNAL STATE ===
    /**
     * Internal map storing unlocked achievements with timestamp.
     */
    private final Map<Achievement, LocalDateTime> unlocked = new HashMap<>();

    // === METHODS ===

    /**
     * Unlocks a specific achievement.
     * <p>
     * If the achievement is already unlocked, the timestamp is preserved.
     * 
     * @param a Achievement to unlock
     * @return true if the achievement was newly unlocked, false if already unlocked
     */
    @SuppressWarnings("unused")
    public boolean unlock(Achievement a) {
        return unlocked.merge(a, LocalDateTime.now(), (oldV, newV) -> oldV) == null;
    }

    /**
     * Checks if an achievement has been unlocked.
     * 
     * @param a Achievement to check
     * @return true if unlocked, false otherwise
     */
    public boolean isUnlocked(Achievement a) {
        return unlocked.containsKey(a);
    }

    /**
     * Returns all achievements that have been unlocked.
     * The returned set is unmodifiable.
     * 
     * @return Set of unlocked achievements
     */
    public Set<Achievement> getUnlocked() {
        return Collections.unmodifiableSet(unlocked.keySet());
    }

    /**
     * Persists the current unlocked achievements to disk.
     * <p>
     * Saves to a temporary file first and then renames to ensure data integrity.
     */
    public void save() {
        Snapshot snap = new Snapshot();
        snap.unlocked.putAll(unlocked);
        File target = new File(FILE);
        File tmp = new File(FILE + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp))) {
            out.writeObject(snap);
            out.flush();
        } catch (IOException e) {
            if (tmp.exists()) tmp.delete();
            return;
        }
        if (target.exists() && !target.delete()) { tmp.delete(); return; }
        if (!tmp.renameTo(target)) { tmp.delete(); }
    }

    /**
     * Loads unlocked achievements from persistent storage.
     * <p>
     * If the file does not exist or an error occurs, the method silently returns.
     */
    public void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof Snapshot) {
                Snapshot s = (Snapshot) obj;
                unlocked.clear();
                if (s.unlocked != null) unlocked.putAll(s.unlocked);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Merges an external snapshot of achievements into the current state.
     * <p>
     * For overlapping achievements, the later timestamp is preserved.
     * 
     * @param s Snapshot to merge
     */
    public void mergeSnapshot(Snapshot s) {
        if (s == null || s.unlocked == null) return;
        for (Map.Entry<Achievement, LocalDateTime> e : s.unlocked.entrySet()) {
            unlocked.merge(e.getKey(), e.getValue(), (cur, inc) -> inc.isAfter(cur) ? inc : cur);
        }
    }

    /**
     * Loads a snapshot from a given file without affecting the current state.
     * 
     * @param f File containing serialized snapshot
     * @return Snapshot object if successfully loaded, null otherwise
     */
    public static Snapshot loadSnapshot(File f) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof Snapshot) return (Snapshot) obj;
        } catch (Exception ignored) {}
        return null;
    }
}
