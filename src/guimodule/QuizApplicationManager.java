package guimodule;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.util.function.*;

/**
 * QuizApplicationManager handles **desktop integration**, **persistent data storage**, 
 * and application lifecycle management for the Quizee application.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Automatic creation of desktop folders for application, data, statistics, and backups</li>
 *     <li>Lambda-based, functional handling of data persistence and backup operations</li>
 *     <li>Creation of a desktop shortcut (Windows-specific)</li>
 *     <li>Logging and lifecycle setup for application initialization</li>
 * </ul>
 * <p>
 * This manager indirectly integrates with **Themes, Questions, Quiz Sessions, and Leitner Cards**
 * by ensuring proper storage and accessibility of configuration and statistics data. 
 * UI panels and logic modules use the stored data to maintain consistency.
 * 
 * <p>
 * Serialization is supported for storing and restoring the manager state.
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class QuizApplicationManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // === Desktop / Folder Constants ===
    private static final String DESKTOP_FOLDER_NAME = "QuizeeApp";
    private static final String DATA_FOLDER_NAME = "data";
    private static final String STATS_FOLDER_NAME = "statistics";
    private static final String BACKUP_FOLDER_NAME = "backups";
    
    // === File Names ===
    private static final String APP_DATA_FILE = "quiz_data.dat";
    private static final String STATS_DATA_FILE = "quiz_statistics.dat";
    private static final String CONFIG_FILE = "app_config.properties";
    
    // === Paths for Application Integration ===
    private Path desktopPath;
    private Path appFolderPath;
    private Path dataFolderPath;
    private Path statsFolderPath;
    private Path backupFolderPath;
    
    // === Lambda-based Functional Operations ===
    private Supplier<Boolean> initializeDirectories;
    private Supplier<Boolean> createDesktopShortcut;
    private Function<String, Boolean> saveApplicationData;
    private Function<String, String> loadApplicationData;
    private Supplier<Boolean> createBackup;
    private Consumer<String> logMessage;
    
    /**
     * Default constructor initializes paths, lambda operations, and sets up the application.
     * Automatically creates required folders and desktop shortcut if needed.
     */
    public QuizApplicationManager() {
        initializePaths();
        initializeLambdas();
        setupApplication();
    }
    
    /**
     * Initializes all application folder paths based on the user's desktop directory.
     * Ensures separate folders for data, statistics, and backups.
     */
    private void initializePaths() {
        try {
            String userHome = System.getProperty("user.home");
            desktopPath = Paths.get(userHome, "Desktop");
            
            appFolderPath = desktopPath.resolve(DESKTOP_FOLDER_NAME);
            dataFolderPath = appFolderPath.resolve(DATA_FOLDER_NAME);
            statsFolderPath = appFolderPath.resolve(STATS_FOLDER_NAME);
            backupFolderPath = appFolderPath.resolve(BACKUP_FOLDER_NAME);
            
        } catch (Exception e) {
            System.err.println("Failed to initialize paths: " + e.getMessage());
        }
    }
    
    /**
     * Initializes all lambda-based operations for directories, shortcuts, persistence, backup, and logging.
     * <p>
     * Lambdas encapsulate controlled side-effects and provide functional programming integration
     * for application management.
     */
    private void initializeLambdas() {
        // Create required directories if they do not exist
        initializeDirectories = () -> {
            try {
                Files.createDirectories(appFolderPath);
                Files.createDirectories(dataFolderPath);
                Files.createDirectories(statsFolderPath);
                Files.createDirectories(backupFolderPath);
                logMessage.accept("Application directories created successfully");
                return true;
            } catch (IOException e) {
                logMessage.accept("Failed to create directories: " + e.getMessage());
                return false;
            }
        };
        
        // Create a desktop shortcut for the application (Windows only)
        createDesktopShortcut = () -> {
            try {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    String shortcutContent = createWindowsShortcut();
                    Path shortcutPath = desktopPath.resolve("Quizee Application.lnk");
                    Files.write(shortcutPath, shortcutContent.getBytes());
                    logMessage.accept("Desktop shortcut created");
                    return true;
                }
                return false;
            } catch (Exception e) {
                logMessage.accept("Failed to create desktop shortcut: " + e.getMessage());
                return false;
            }
        };
        
        // Save application data to persistent storage
        saveApplicationData = data -> {
            try {
                Path dataFile = dataFolderPath.resolve(APP_DATA_FILE);
                Files.write(dataFile, data.getBytes());
                logMessage.accept("Application data saved");
                return true;
            } catch (IOException e) {
                logMessage.accept("Failed to save application data: " + e.getMessage());
                return false;
            }
        };
        
        // Load application data from persistent storage
        loadApplicationData = filename -> {
            try {
                Path dataFile = dataFolderPath.resolve(filename);
                if (Files.exists(dataFile)) {
                    return Files.readString(dataFile);
                }
                return "";
            } catch (IOException e) {
                logMessage.accept("Failed to load application data: " + e.getMessage());
                return "";
            }
        };
        
        // Create backup of data and statistics
        createBackup = () -> {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                Path backupDir = backupFolderPath.resolve("backup_" + timestamp);
                Files.createDirectories(backupDir);
                
                // Copy main data files
                if (Files.exists(dataFolderPath.resolve(APP_DATA_FILE))) {
                    Files.copy(
                        dataFolderPath.resolve(APP_DATA_FILE),
                        backupDir.resolve(APP_DATA_FILE)
                    );
                }
                if (Files.exists(statsFolderPath.resolve(STATS_DATA_FILE))) {
                    Files.copy(
                        statsFolderPath.resolve(STATS_DATA_FILE),
                        backupDir.resolve(STATS_DATA_FILE)
                    );
                }
                
                logMessage.accept("Backup created: " + backupDir.getFileName());
                return true;
            } catch (IOException e) {
                logMessage.accept("Failed to create backup: " + e.getMessage());
                return false;
            }
        };
        
        // Simple logging lambda for consistent console output
        logMessage = message -> System.out.println("[QuizApp Manager] " + message);
    }
    
    /**
     * Sets up the application by creating directories and desktop shortcut if missing.
     * Ensures a ready-to-use environment for Themes, Questions, Sessions, and Leitner Cards.
     */
    private void setupApplication() {
        if (initializeDirectories.get()) {
            logMessage.accept("Application setup completed successfully");
            Path shortcutPath = desktopPath.resolve("Quizee Application.lnk");
            if (!Files.exists(shortcutPath)) {
                createDesktopShortcut.get();
            }
        }
    }
    
    /**
     * Creates a simplified Windows batch shortcut to launch the application.
     * <p>
     * Note: This is a basic implementation. Proper .lnk files would require JNI or external tools.
     */
    private String createWindowsShortcut() {
        return String.format(
            "@echo off\n" +
            "cd /d \"%s\"\n" +
            "java -cp . guimodule.QuizApplicationDemo\n" +
            "pause",
            System.getProperty("user.dir")
        );
    }
    
    // === Public API ===
    
    public Path getAppFolderPath() { return appFolderPath; }
    public Path getDataFolderPath() { return dataFolderPath; }
    public Path getStatsFolderPath() { return statsFolderPath; }
    public Path getBackupFolderPath() { return backupFolderPath; }
    
    public boolean saveData(String data) { return saveApplicationData.apply(data); }
    public String loadData(String filename) { return loadApplicationData.apply(filename); }
    public boolean createBackup() { return createBackup.get(); }
    
    /**
     * Opens the main application folder in the system file explorer.
     * Ensures user can quickly access data, statistics, or backups.
     */
    public void openAppFolder() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(appFolderPath.toFile());
            }
        } catch (IOException e) {
            logMessage.accept("Failed to open app folder: " + e.getMessage());
        }
    }
}