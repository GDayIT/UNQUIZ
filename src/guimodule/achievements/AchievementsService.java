package guimodule.achievements;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AchievementsService manages unlocking and persistence of user achievements.
 * It is independent from questions, Leitner and statistics, but can be updated
 * by those modules via events.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class AchievementsService implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE = "achievements.dat";

    public enum Achievement {
        FIRST_CORRECT_ANSWER,
        TEN_CORRECT_IN_A_ROW,
        FIRST_THEME_CREATED,
        TEN_QUESTIONS_CREATED,
        FIRST_LEITNER_REVIEW,
        LEITNER_LEVEL6_ACHIEVED
    }

    public static class Entry implements Serializable {
        public final Achievement achievement;
        public final LocalDateTime unlockedAt;
        public Entry(Achievement a, LocalDateTime t) { this.achievement = a; this.unlockedAt = t; }
    }

    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        public Map<Achievement, LocalDateTime> unlocked = new HashMap<>();
    }

    private final Map<Achievement, LocalDateTime> unlocked = new HashMap<>();

    public boolean unlock(Achievement a) {
        return unlocked.merge(a, LocalDateTime.now(), (oldV, newV) -> oldV) == null;
    }

    public boolean isUnlocked(Achievement a) {
        return unlocked.containsKey(a);
    }

    public Set<Achievement> getUnlocked() {
        return Collections.unmodifiableSet(unlocked.keySet());
    }

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

    public void mergeSnapshot(Snapshot s) {
        if (s == null || s.unlocked == null) return;
        for (Map.Entry<Achievement, LocalDateTime> e : s.unlocked.entrySet()) {
            unlocked.merge(e.getKey(), e.getValue(), (cur, inc) -> inc.isAfter(cur) ? inc : cur);
        }
    }

    public static Snapshot loadSnapshot(File f) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof Snapshot) return (Snapshot) obj;
        } catch (Exception ignored) {}
        return null;
    }
}
