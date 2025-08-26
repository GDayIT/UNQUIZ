package dbbl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

/**
 * Robuste DAO/DTO-Version für MariaDB + Swing.
 * Automatischer Verbindungs-Retry, Exceptions sauber behandelt.
 * Antworten in separater Tabelle, Fragen in questions, Themes in themes.
 */
public class DatenBankDAOxDTO {

    private static final String URL = "jdbc:mariadb://localhost:3306/georgiou_DATA";
    private static final String HOST = "localhost";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public DatenBankDAOxDTO() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            showError("MariaDB-Treiber nicht gefunden", e);
        }
    }

    // ========================= CONNECTION =========================
    private Connection getConnection() throws SQLException {
        SQLException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return DriverManager.getConnection(HOST, USER, PASSWORD);
            } catch (SQLException e) {
                lastException = e;
                System.err.println("DB-Verbindung fehlgeschlagen, Versuch " + attempt + " von " + MAX_RETRIES);
                try { TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }
        throw lastException;
    }

    // ========================= THEME METHODS =========================
    public boolean saveTheme(String title, String description) {
        if (title == null || title.isBlank()) return false;
        String sqlCheck = "SELECT id FROM themes WHERE title=?";
        String sqlInsert = "INSERT INTO themes(title, description) VALUES(?, ?)";
        String sqlUpdate = "UPDATE themes SET description=? WHERE title=?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, title);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                        psUpdate.setString(1, description);
                        psUpdate.setString(2, title);
                        psUpdate.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, title);
                        psInsert.setString(2, description);
                        psInsert.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            showError("Fehler beim Speichern des Themes", e);
            return false;
        }
    }

    public boolean deleteTheme(String title) {
        String sql = "DELETE FROM themes WHERE title=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError("Fehler beim Löschen des Themes", e);
            return false;
        }
    }

    public Map<String, String> loadTheme(String title) {
        String sql = "SELECT * FROM themes WHERE title=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> theme = new HashMap<>();
                theme.put("title", rs.getString("title"));
                theme.put("description", rs.getString("description"));
                return theme;
            }
        } catch (SQLException e) {
            showError("Fehler beim Laden des Themes", e);
        }
        return null;
    }

    public List<Map<String, String>> getAllThemes() {
        String sql = "SELECT * FROM themes";
        List<Map<String, String>> themes = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> theme = new HashMap<>();
                theme.put("title", rs.getString("title"));
                theme.put("description", rs.getString("description"));
                themes.add(theme);
            }
        } catch (SQLException e) {
            showError("Fehler beim Laden aller Themes", e);
        }
        return themes;
    }

    // ========================= QUESTION METHODS =========================
    public boolean saveQuestion(String themeTitle, String title, String text, String explanation,
                                List<String> answers, List<Boolean> correctFlags) {
        String sqlTheme = "SELECT id FROM themes WHERE title=?";
        String sqlInsertQuestion = "INSERT INTO questions(theme_id, title, text, explanation) VALUES(?,?,?,?)";
        String sqlUpdateQuestion = "UPDATE questions SET text=?, explanation=? WHERE theme_id=? AND title=?";
        String sqlDeleteAnswers = "DELETE FROM answers WHERE question_id=?";
        String sqlInsertAnswer = "INSERT INTO answers(question_id, answer_text, is_correct) VALUES(?,?,?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Transaktion starten

            // Theme-ID holen
            int themeId;
            try (PreparedStatement psTheme = conn.prepareStatement(sqlTheme)) {
                psTheme.setString(1, themeTitle);
                ResultSet rsTheme = psTheme.executeQuery();
                if (!rsTheme.next()) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "Theme existiert nicht: " + themeTitle, "Warnung", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                themeId = rsTheme.getInt("id");
            }

            // Frage prüfen
            int questionId;
            String sqlCheck = "SELECT id FROM questions WHERE theme_id=? AND title=?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, themeId);
                psCheck.setString(2, title);
                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next()) {
                    questionId = rsCheck.getInt("id");
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateQuestion)) {
                        psUpdate.setString(1, text);
                        psUpdate.setString(2, explanation);
                        psUpdate.setInt(3, themeId);
                        psUpdate.setString(4, title);
                        psUpdate.executeUpdate();
                    }
                    // Alte Antworten löschen
                    try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteAnswers)) {
                        psDel.setInt(1, questionId);
                        psDel.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertQuestion, Statement.RETURN_GENERATED_KEYS)) {
                        psInsert.setInt(1, themeId);
                        psInsert.setString(2, title);
                        psInsert.setString(3, text);
                        psInsert.setString(4, explanation);
                        psInsert.executeUpdate();
                        ResultSet keys = psInsert.getGeneratedKeys();
                        keys.next();
                        questionId = keys.getInt(1);
                    }
                }
            }

            // Neue Antworten einfügen
            for (int i = 0; i < answers.size(); i++) {
                try (PreparedStatement psAns = conn.prepareStatement(sqlInsertAnswer)) {
                    psAns.setInt(1, questionId);
                    psAns.setString(2, answers.get(i));
                    psAns.setBoolean(3, correctFlags.get(i));
                    psAns.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            showError("Fehler beim Speichern der Frage", e);
            return false;
        }
    }

    public List<Map<String, Object>> loadQuestionsByTheme(String themeTitle) {
        String sqlTheme = "SELECT id FROM themes WHERE title=?";
        String sqlQuestions = "SELECT * FROM questions WHERE theme_id=?";
        String sqlAnswers = "SELECT * FROM answers WHERE question_id=?";
        List<Map<String, Object>> questions = new ArrayList<>();

        try (Connection conn = getConnection()) {
            int themeId;
            try (PreparedStatement psTheme = conn.prepareStatement(sqlTheme)) {
                psTheme.setString(1, themeTitle);
                ResultSet rsTheme = psTheme.executeQuery();
                if (!rsTheme.next()) return questions;
                themeId = rsTheme.getInt("id");
            }

            try (PreparedStatement psQuestions = conn.prepareStatement(sqlQuestions)) {
                psQuestions.setInt(1, themeId);
                ResultSet rsQuestions = psQuestions.executeQuery();

                while (rsQuestions.next()) {
                    int qId = rsQuestions.getInt("id");
                    Map<String, Object> question = new HashMap<>();
                    question.put("title", rsQuestions.getString("title"));
                    question.put("text", rsQuestions.getString("text"));
                    question.put("explanation", rsQuestions.getString("explanation"));

                    List<String> answers = new ArrayList<>();
                    List<Boolean> correctFlags = new ArrayList<>();
                    try (PreparedStatement psAns = conn.prepareStatement(sqlAnswers)) {
                        psAns.setInt(1, qId);
                        ResultSet rsAns = psAns.executeQuery();
                        while (rsAns.next()) {
                            answers.add(rsAns.getString("answer_text"));
                            correctFlags.add(rsAns.getBoolean("is_correct"));
                        }
                    }
                    question.put("answers", answers);
                    question.put("correctFlags", correctFlags);
                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            showError("Fehler beim Laden der Fragen", e);
        }

        return questions;
    }

    public boolean deleteQuestion(String themeTitle, String questionTitle) {
        String sql = "DELETE q FROM questions q JOIN themes t ON q.theme_id=t.id WHERE t.title=? AND q.title=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, themeTitle);
            ps.setString(2, questionTitle);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError("Fehler beim Löschen der Frage", e);
            return false;
        }
    }

    // ========================= UTILITY =========================
    private void showError(String msg, Exception e) {
        JOptionPane.showMessageDialog(null, msg + ":\n" + e.getMessage(), "Datenbank-Fehler", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
