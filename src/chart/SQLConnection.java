package chart;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnection {

    private static SQLConnection instance;
    private Connection connection;

    private String url;
    private String user;
    private String password;

    private static final String CONFIG_FILE = "db.properties";

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────
    private SQLConnection() {
        loadFromFile();
        connect();
    }

    // ─────────────────────────────────────────────
    // Load DB config from file
    // ─────────────────────────────────────────────
    private void loadFromFile() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");

            System.out.println("Using DB URL: " + url);

        } catch (Exception e) {
            System.out.println("⚠ No config file found. Using default MySQL settings.");

            // Default fallback
            this.url = "jdbc:mysql://localhost:3306/accountdb";
            this.user = "root";
            this.password = "";
        }
    }

    // ─────────────────────────────────────────────
    // Save settings (call this from Settings page)
    // ─────────────────────────────────────────────
    public static void saveSettings(String url, String user, String password) {
        Properties props = new Properties();
        props.setProperty("db.url", url);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Database Configuration");
            System.out.println("✅ Settings saved.");

            // Force reconnection with new settings
            reset();

        } catch (Exception e) {
            System.err.println("❌ Failed to save settings.");
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Establish connection
    // ─────────────────────────────────────────────
    private void connect() {
        try {
            loadDriver();

            DriverManager.setLoginTimeout(5);
            connection = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database connected successfully!");

        } catch (Exception e) {
            System.err.println("❌ Database connection failed.");
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Load correct driver based on URL
    // ─────────────────────────────────────────────
    private void loadDriver() throws ClassNotFoundException {
        if (url == null) {
            throw new ClassNotFoundException("Database URL is null.");
        }

        if (url.startsWith("jdbc:mysql")) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Loaded MySQL Driver");
        } else if (url.startsWith("jdbc:oracle")) {
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Loaded Oracle Driver");
        } else {
            throw new ClassNotFoundException("Unsupported DB type: " + url);
        }
    }

    // ─────────────────────────────────────────────
    // Singleton access
    // ─────────────────────────────────────────────
    public static SQLConnection getInstance() {
        if (instance == null) {
            instance = new SQLConnection();
        }
        return instance;
    }

    // ─────────────────────────────────────────────
    // Get active connection
    // ─────────────────────────────────────────────
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnecting to database...");
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            connect();
        }
        return connection;
    }

    // ─────────────────────────────────────────────
    // Reset instance (IMPORTANT for Settings page)
    // ─────────────────────────────────────────────
    public static void reset() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("Old connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        instance = null;
    }
}