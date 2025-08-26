package guimodule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Simple application configuration service.
 *
 * Supports:
 * - application.properties in the current working directory (user.dir)
 * - CLI args in the form --key=value (e.g., --merge=quiz)
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public final class AppConfigService {

    private final Properties props = new Properties();
    private final Map<String, String> argsMap = new HashMap<>();

    public AppConfigService(String[] args) {
        loadArgs(args);
        loadProperties();
    }

    private void loadArgs(String[] args) {
        if (args == null) return;
        for (String a : args) {
            if (a == null) continue;
            if (a.startsWith("--")) {
                String kv = a.substring(2);
                int idx = kv.indexOf('=');
                if (idx > 0) {
                    String k = kv.substring(0, idx).trim();
                    String v = kv.substring(idx + 1).trim();
                    if (!k.isEmpty()) argsMap.put(k, v);
                } else {
                    argsMap.put(kv.trim(), "true");
                }
            }
        }
    }

    private void loadProperties() {
        String base = System.getProperty("user.dir");
        File f = new File(base, "application.properties");
        if (!f.exists()) return;
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
        } catch (IOException ignored) {}
    }

    public String getString(String key, String def) {
        String v = argsMap.get(key);
        if (v != null) return v;
        v = props.getProperty(key);
        return v != null ? v : def;
    }

    public boolean getBoolean(String key, boolean def) {
        String v = getString(key, null);
        if (v == null) return def;
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("1") || v.equalsIgnoreCase("yes");
    }
}
