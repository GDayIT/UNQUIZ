package dbbl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import guimodule.AdaptiveLeitnerCard;

/**
 * The {@code DatenBankDAOxDTO} class is a Data Access Object (DAO) and 
 * Data Transfer Object (DTO) responsible for persisting and retrieving 
 * data for a Quiz system with Adaptive Leitner Cards.
 * 
 * <p>This class handles:
 * <ul>
 *   <li>Themes (CRUD operations)</li>
 *   <li>Questions & Answers (CRUD with batch insert)</li>
 *   <li>Adaptive Leitner Cards (CRUD and performance metrics)</li>
 *   <li>Session registration and management</li>
 * </ul>
 * 
 * <p><b>Connection Handling:</b>
 * <ul>
 *   <li>Uses try-with-resources to ensure connections are always closed</li>
 *   <li>Write operations disable AutoCommit and explicitly commit/rollback</li>
 *   <li>Read operations keep AutoCommit true and close immediately</li>
 * </ul>
 * 
 * <p><b>Error Handling:</b>
 * <ul>
 *   <li>Database errors are displayed to the user via {@link JOptionPane}</li>
 *   <li>Transactions are rolled back in case of exceptions</li>
 * </ul>
 * 
 * <p><b>Dependencies:</b>
 * <ul>
 *   <li>{@link java.sql.Connection}, {@link PreparedStatement}, {@link ResultSet}, {@link Statement}</li>
 *   <li>{@link java.sql.Date} and {@link java.sql.Timestamp} for temporal values</li>
 *   <li>{@link guimodule.AdaptiveLeitnerCard} for Leitner Card domain objects</li>
 *   <li>{@link javax.swing.JOptionPane} for error dialogs</li>
 * </ul>
 * 
 * @author D.Georgiou
 * @version 1.0
 */
public class DatenBankDAOxDTO {

    // ------------------- DATABASE CONNECTION FIELDS -------------------

    /** JDBC URL for connecting to the MariaDB database */
    private final String url = "jdbc:mariadb://localhost:3306/dgquizdata";

    /** Database username */
    private final String user = "root";

    /** Database password (empty) */
    private final String password = "";

    // ------------------- CONNECTION HANDLING -------------------

    /**
     * Creates a new database connection using configured credentials.
     *
     * @return a valid {@link Connection} object
     * @throws SQLException if the connection cannot be established
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Displays an error message to the user in a dialog box.
     *
     * @param message the message to display
     */
    private void handleError(String message) {
        JOptionPane.showMessageDialog(null, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------- THEMES -------------------

    /**
     * Saves a new theme or updates the description if it already exists.
     *
     * @param title the theme title
     * @param description the theme description
     * @return {@code true} if successful, {@code false} if an error occurred
     */
    public boolean saveTheme(String title, String description) {
        String sql = "INSERT INTO themes(title, description) VALUES(?, ?) ON DUPLICATE KEY UPDATE description=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, description);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            handleError("saveTheme failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all theme titles from the database.
     *
     * @return a list of theme titles
     */
    public List<String> loadAllThemes() {
        List<String> themes = new ArrayList<>();
        String sql = "SELECT title FROM themes";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                themes.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            handleError("loadAllThemes failed: " + e.getMessage());
        }
        return themes;
    }

    /**
     * Deletes a theme by its title.
     *
     * @param title the theme title to delete
     * @return {@code true} if successful, {@code false} if an error occurred
     */
    public boolean deleteTheme(String title) {
        String sql = "DELETE FROM themes WHERE title=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            handleError("deleteTheme failed: " + e.getMessage());
            return false;
        }
    }

    // ------------------- QUESTIONS & ANSWERS -------------------

    /**
     * Saves a question and its associated answers. Updates existing question if duplicate.
     * Batch inserts are used for answers to improve performance.
     *
     * @param themeId the theme ID
     * @param title the question title
     * @param text the question text
     * @param explanation optional explanation
     * @param answers list of answer texts
     * @param correctFlags parallel list of booleans indicating correct answers
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean saveQuestion(int themeId, String title, String text, String explanation,
                                List<String> answers, List<Boolean> correctFlags) {
        String questionSql = "INSERT INTO questions(theme_id, title, text, explanation) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE text=?, explanation=?";
        String answerSql = "INSERT INTO answers(question_id, answer_text, is_correct) VALUES(?,?,?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int questionId;
            try (PreparedStatement ps = conn.prepareStatement(questionSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, themeId);
                ps.setString(2, title);
                ps.setString(3, text);
                ps.setString(4, explanation);
                ps.setString(5, text);
                ps.setString(6, explanation);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) questionId = rs.getInt(1);
                    else questionId = getQuestionIdByThemeAndTitle(conn, themeId, title);
                }
            }

            try (PreparedStatement psAnswer = conn.prepareStatement(answerSql)) {
                for (int i = 0; i < answers.size(); i++) {
                    psAnswer.setInt(1, questionId);
                    psAnswer.setString(2, answers.get(i));
                    psAnswer.setBoolean(3, correctFlags.get(i));
                    psAnswer.addBatch();
                }
                psAnswer.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleError("saveQuestion failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the ID of a question by theme and title.
     *
     * @param conn active database connection
     * @param themeId theme ID
     * @param title question title
     * @return question ID, or -1 if not found
     * @throws SQLException if a database error occurs
     */
    private int getQuestionIdByThemeAndTitle(Connection conn, int themeId, String title) throws SQLException {
        String sql = "SELECT id FROM questions WHERE theme_id=? AND title=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, themeId);
            ps.setString(2, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    // ------------------- LEITNER CARDS -------------------

    /**
     * Saves or updates an Adaptive Leitner Card.
     * If the card exists, it updates; otherwise, it inserts a new record.
     *
     * @param card the Leitner card
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean saveLeitnerCard(AdaptiveLeitnerCard card) {
        String checkSql = "SELECT id FROM leitner_cards WHERE question_id=?";
        String insertSql = "INSERT INTO leitner_cards(question_id, box, consecutive_correct, consecutive_wrong, total_attempts, total_correct, average_response_time, next_review, difficulty, last_reviewed) VALUES(?,?,?,?,?,?,?,?,?,?)";
        String updateSql = "UPDATE leitner_cards SET box=?, consecutive_correct=?, consecutive_wrong=?, total_attempts=?, total_correct=?, average_response_time=?, next_review=?, difficulty=?, last_reviewed=? WHERE question_id=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int themeId = getThemeIdByName(card.getTheme(), conn);
            int questionId = getQuestionIdByThemeAndTitle(conn, themeId, card.getQuestionTitle());
            if (questionId == -1) {
                handleError("Question not found for Leitner card: " + card.getQuestionId());
                return false;
            }

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, questionId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setInt(1, card.getBox());
                            psUpdate.setInt(2, card.getConsecutiveCorrect());
                            psUpdate.setInt(3, card.getConsecutiveWrong());
                            psUpdate.setInt(4, card.getTotalAttempts());
                            psUpdate.setInt(5, card.getTotalCorrect());
                            psUpdate.setDouble(6, card.getAverageResponseTime());
                            psUpdate.setDate(7, Date.valueOf(card.getNextReviewDate()));
                            psUpdate.setString(8, card.getDifficulty().name());
                            psUpdate.setTimestamp(9, card.getLastReviewed() != null ? Timestamp.valueOf(card.getLastReviewed()) : null);
                            psUpdate.setInt(10, questionId);
                            psUpdate.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                            psInsert.setInt(1, questionId);
                            psInsert.setInt(2, card.getBox());
                            psInsert.setInt(3, card.getConsecutiveCorrect());
                            psInsert.setInt(4, card.getConsecutiveWrong());
                            psInsert.setInt(5, card.getTotalAttempts());
                            psInsert.setInt(6, card.getTotalCorrect());
                            psInsert.setDouble(7, card.getAverageResponseTime());
                            psInsert.setDate(8, Date.valueOf(card.getNextReviewDate()));
                            psInsert.setString(9, card.getDifficulty().name());
                            psInsert.setTimestamp(10, card.getLastReviewed() != null ? Timestamp.valueOf(card.getLastReviewed()) : null);
                            psInsert.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            handleError("saveLeitnerCard failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to retrieve a theme ID by its title.
     *
     * @param themeName theme title
     * @param conn active connection
     * @return theme ID, or -1 if not found
     * @throws SQLException database error
     */
    private int getThemeIdByName(String themeName, Connection conn) throws SQLException {
        String sql = "SELECT id FROM themes WHERE title=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, themeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Loads all Leitner cards including their theme and question titles.
     *
     * @return a list of AdaptiveLeitnerCard
     */
    public List<AdaptiveLeitnerCard> loadAllLeitnerCards() {
        List<AdaptiveLeitnerCard> cards = new ArrayList<>();
        String sql = "SELECT lc.*, q.title AS question_title, t.id AS theme_id, t.title AS theme_title " +
                     "FROM leitner_cards lc " +
                     "JOIN questions q ON lc.question_id = q.id " +
                     "JOIN themes t ON q.theme_id = t.id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("last_reviewed");
                Date nextReview = rs.getDate("next_review");

                AdaptiveLeitnerCard card = new AdaptiveLeitnerCard(
                    rs.getInt("question_id") + ":" + rs.getString("question_title"),
                    rs.getString("theme_title"),
                    rs.getString("question_title"),
                    rs.getInt("box"),
                    AdaptiveLeitnerCard.Difficulty.valueOf(rs.getString("difficulty")),
                    rs.getInt("consecutive_correct"),
                    rs.getInt("consecutive_wrong"),
                    rs.getInt("total_attempts"),
                    rs.getInt("total_correct"),
                    rs.getDouble("average_response_time"),
                    ts != null ? ts.toLocalDateTime() : null,
                    nextReview != null ? nextReview.toLocalDate() : LocalDate.now()
                );

                cards.add(card);
            }

        } catch (SQLException e) {
            handleError("loadAllLeitnerCards failed: " + e.getMessage());
        }

        return cards;
    }

    // ------------------- SESSION MANAGEMENT -------------------

    /**
     * Registers a new session with a unique instance ID.
     * Maximum concurrent sessions limited to 300.
     *
     * @param instanceId unique session identifier
     * @return true if registration succeeded, false if limit reached
     */
    public boolean registerSession(String instanceId) {
        try (Connection conn = getConnection()) {
            String countSql = "SELECT COUNT(*) FROM active_sessions";
            int count;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                rs.next();
                count = rs.getInt(1);
            }

            if (count >= 300) return false;

            String insertSql = "INSERT INTO active_sessions(instance_id) VALUES(?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, instanceId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unregisters a session by its instance ID.
     *
     * @param instanceId session identifier
     */
    public void unregisterSession(String instanceId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM active_sessions WHERE instance_id = ?")) {
            ps.setString(1, instanceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}