package chart; // Declare the package name for this class

import java.io.FileInputStream; // Import FileInputStream for reading configuration files
import java.io.FileOutputStream; // Import FileOutputStream for writing configuration files
import java.sql.Connection; // Import Connection interface for database connections
import java.sql.DriverManager; // Import DriverManager for managing JDBC drivers
import java.sql.SQLException; // Import SQLException for handling SQL errors
import java.util.Properties; // Import Properties for handling key-value configuration data

public class SQLConnection { // Define the SQLConnection class

    private static SQLConnection instance; // Static instance for singleton pattern
    private Connection connection; // Instance variable to hold the database connection

    private String url; // Database URL string
    private String user; // Database username
    private String password; // Database password

    private static final String CONFIG_FILE = "db.properties"; // Constant for config file name

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────
    private SQLConnection() { // Private constructor for singleton pattern
        loadFromFile(); // Load database configuration from file
        connect(); // Establish database connection
    }

    // ─────────────────────────────────────────────
    // Load DB config from file
    // ─────────────────────────────────────────────
    private void loadFromFile() { // Method to load database configuration from properties file
        Properties props = new Properties(); // Create Properties object to hold config data

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) { // Open config file for reading
            props.load(fis); // Load properties from the file

            this.url = props.getProperty("db.url"); // Get database URL from properties
            this.user = props.getProperty("db.user"); // Get database username from properties
            this.password = props.getProperty("db.password"); // Get database password from properties

            System.out.println("Using DB URL: " + url); // Print the loaded URL to console

        } catch (Exception e) { // Catch any exceptions during file loading
            System.out.println("⚠ No config file found. Using default MySQL settings."); // Warn user about missing config

            // Default fallback
            this.url = "jdbc:mysql://localhost:3306/accountdb"; // Set default MySQL URL
            this.user = "root"; // Set default username
            this.password = ""; // Set default empty password
        }
    }

    // ─────────────────────────────────────────────
    // Save settings (call this from Settings page)
    // ─────────────────────────────────────────────
    public static void saveSettings(String url, String user, String password) { // Static method to save database settings to file
        Properties props = new Properties(); // Create Properties object for config data
        props.setProperty("db.url", url); // Set database URL in properties
        props.setProperty("db.user", user); // Set database username in properties
        props.setProperty("db.password", password); // Set database password in properties

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) { // Open config file for writing
            props.store(fos, "Database Configuration"); // Save properties to file with comment
            System.out.println("✅ Settings saved."); // Confirm settings were saved

            // Force reconnection with new settings
            reset(); // Reset the singleton instance to apply new settings

        } catch (Exception e) { // Catch any exceptions during saving
            System.err.println("❌ Failed to save settings."); // Print error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // ─────────────────────────────────────────────
    // Establish connection
    // ─────────────────────────────────────────────
    private void connect() { // Method to establish database connection
        try {
            loadDriver(); // Load the appropriate JDBC driver

            DriverManager.setLoginTimeout(5); // Set login timeout to 5 seconds
            connection = DriverManager.getConnection(url, user, password); // Create database connection

            System.out.println("✅ Database connected successfully!"); // Confirm successful connection

        } catch (Exception e) { // Catch any exceptions during connection
            System.err.println("❌ Database connection failed."); // Print error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // ─────────────────────────────────────────────
    // Load correct driver based on URL
    // ─────────────────────────────────────────────
    private void loadDriver() throws ClassNotFoundException { // Method to load JDBC driver based on URL
        if (url == null) { // Check if URL is null
            throw new ClassNotFoundException("Database URL is null."); // Throw exception if URL is null
        }

        if (url.startsWith("jdbc:mysql")) { // Check if URL is for MySQL
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver
            System.out.println("Loaded MySQL Driver"); // Confirm MySQL driver loaded
        } else if (url.startsWith("jdbc:oracle")) { // Check if URL is for Oracle
            Class.forName("oracle.jdbc.OracleDriver"); // Load Oracle JDBC driver
            System.out.println("Loaded Oracle Driver"); // Confirm Oracle driver loaded
        } else {
            throw new ClassNotFoundException("Unsupported DB type: " + url); // Throw exception for unsupported database
        }
    }

    // ─────────────────────────────────────────────
    // Singleton access
    // ─────────────────────────────────────────────
    public static SQLConnection getInstance() { // Static method to get singleton instance
        if (instance == null) { // Check if instance is not created yet
            instance = new SQLConnection(); // Create new instance if null
        }
        return instance; // Return the singleton instance
    }

    // ─────────────────────────────────────────────
    // Get active connection
    // ─────────────────────────────────────────────
    public Connection getConnection() { // Method to get the active database connection
        try {
            if (connection == null || connection.isClosed()) { // Check if connection is null or closed
                System.out.println("Reconnecting to database..."); // Print reconnection message
                connect(); // Reconnect to database
            }
        } catch (SQLException e) { // Catch SQL exceptions
            e.printStackTrace(); // Print stack trace
            connect(); // Attempt to reconnect
        }
        return connection; // Return the connection object
    }

    // ─────────────────────────────────────────────
    // Reset instance (IMPORTANT for Settings page)
    // ─────────────────────────────────────────────
    public static void reset() { // Static method to reset the singleton instance
        if (instance != null && instance.connection != null) { // Check if instance and connection exist
            try {
                instance.connection.close(); // Close the current connection
                System.out.println("Old connection closed."); // Confirm connection closed
            } catch (SQLException e) { // Catch SQL exceptions
                e.printStackTrace(); // Print stack trace
            }
        }
        instance = null; // Set instance to null to force recreation
    }
}