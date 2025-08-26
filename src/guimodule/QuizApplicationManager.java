package guimodule;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.util.function.*;

/**
 * Quiz Application Manager für Desktop-Integration und Persistierung.
 * 
 * Features:
 * - Automatische Desktop-Ordner-Erstellung
 * - Persistente Daten-Speicherung
 * - Desktop-Shortcut-Erstellung
 * - Anwendungs-Lifecycle-Management
 * 
<<<<<<< HEAD
 * @author D.Georgiou
=======
 * @author Quiz Application Team
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 * @version 1.0
 */
public class QuizApplicationManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Desktop paths
    private static final String DESKTOP_FOLDER_NAME = "QuizeeApp";
    private static final String DATA_FOLDER_NAME = "data";
    private static final String STATS_FOLDER_NAME = "statistics";
    private static final String BACKUP_FOLDER_NAME = "backups";
    
    // File names
    private static final String APP_DATA_FILE = "quiz_data.dat";
    private static final String STATS_DATA_FILE = "quiz_statistics.dat";
    private static final String CONFIG_FILE = "app_config.properties";
    
    // Paths
    private Path desktopPath;
    private Path appFolderPath;
    private Path dataFolderPath;
    private Path statsFolderPath;
    private Path backupFolderPath;
    
    // Lambda-based operations
    private Supplier<Boolean> initializeDirectories;
    private Supplier<Boolean> createDesktopShortcut;
    private Function<String, Boolean> saveApplicationData;
    private Function<String, String> loadApplicationData;
    private Supplier<Boolean> createBackup;
    private Consumer<String> logMessage;
    
    public QuizApplicationManager() {
        initializePaths();
        initializeLambdas();
        setupApplication();
    }
    
    private void initializePaths() {
        try {
            // Get user's desktop path
            String userHome = System.getProperty("user.home");
            desktopPath = Paths.get(userHome, "Desktop");
            
            // Create application folder structure
            appFolderPath = desktopPath.resolve(DESKTOP_FOLDER_NAME);
            dataFolderPath = appFolderPath.resolve(DATA_FOLDER_NAME);
            statsFolderPath = appFolderPath.resolve(STATS_FOLDER_NAME);
            backupFolderPath = appFolderPath.resolve(BACKUP_FOLDER_NAME);
            
        } catch (Exception e) {
            System.err.println("Failed to initialize paths: " + e.getMessage());
        }
    }
    
    private void initializeLambdas() {
        // Initialize directories
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
        
        // Create desktop shortcut (Windows-specific)
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
        
        // Save application data
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
        
        // Load application data
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
        
        // Create backup
        createBackup = () -> {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                Path backupDir = backupFolderPath.resolve("backup_" + timestamp);
                Files.createDirectories(backupDir);
                
                // Copy data files to backup
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
        
        // Log message
        logMessage = message -> {
            System.out.println("[QuizApp Manager] " + message);
        };
    }
    
    private void setupApplication() {
        // Initialize directories
        if (initializeDirectories.get()) {
            logMessage.accept("Application setup completed successfully");
            
            // Create desktop shortcut if it doesn't exist
            Path shortcutPath = desktopPath.resolve("Quizee Application.lnk");
            if (!Files.exists(shortcutPath)) {
                createDesktopShortcut.get();
            }
        }
    }
    
    private String createWindowsShortcut() {
        // This is a simplified approach - in a real application, 
        // you would use JNI or external tools to create proper .lnk files
        return String.format(
            "@echo off\n" +
            "cd /d \"%s\"\n" +
            "java -cp . guimodule.QuizApplicationDemo\n" +
            "pause",
            System.getProperty("user.dir")
        );
    }
    
    // Public API methods
    public Path getAppFolderPath() { return appFolderPath; }
    public Path getDataFolderPath() { return dataFolderPath; }
    public Path getStatsFolderPath() { return statsFolderPath; }
    public Path getBackupFolderPath() { return backupFolderPath; }
    
    public boolean saveData(String data) { return saveApplicationData.apply(data); }
    public String loadData(String filename) { return loadApplicationData.apply(filename); }
    public boolean createBackup() { return createBackup.get(); }
    
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
