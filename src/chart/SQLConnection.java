package chart; // Declares this class belongs to the 'chart' package

// Import JDBC classes for database connection
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing MySQL database connection
 */
public class SQLConnection {

    // Static instance for Singleton pattern
    private static SQLConnection instance;

    // JDBC connection object
    private Connection connection;

    // Database URL (host, port, database name)
    private static final String URL =
            "jdbc:mysql://localhost:3306/accountdb";

    // Database username
    private static final String USER = "root";

    // Database password
    private static final String PASSWORD = "";

    /**
     * Private constructor to prevent direct object creation
     */
    private SQLConnection() {

        // Establish connection when object is created
        connect();
    }

    /**
     * Establish connection to database
     */
    private void connect() {

        try {

            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Set timeout for login attempt (5 seconds)
            DriverManager.setLoginTimeout(5);

            // Create connection using URL, username, password
            connection = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            // Print success message
            System.out.println("Database connected.");

        } catch (Exception e) {

            // Print failure message
            System.err.println("Database connection failed.");

            // Print error details
            e.printStackTrace();
        }
    }

    /**
     * Get singleton instance of this class
     */
    public static SQLConnection getInstance() {

        // Create instance if it doesn't exist
        if (instance == null) {
            instance = new SQLConnection();
        }

        // Return existing instance
        return instance;
    }

    /**
     * Get active database connection
     */
    public Connection getConnection() {

        try {

            // If connection is null or closed, reconnect
            if (connection == null || connection.isClosed()) {
                connect();
            }

        } catch (SQLException e) {

            // If error occurs, try reconnecting again
            connect();
        }

        // Return active connection
        return connection;
    }
}