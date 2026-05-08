package chart;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLConnection {
    private static SQLConnection instance;
    private Connection connection;

    private SQLConnection() {
        connect();
    }

    // Helper method to establish connection
    private void connect() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@LAPTOP-0DI29GMV:1521:xe", "system", "password");
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static SQLConnection getInstance() {
        if (instance == null) {
            instance = new SQLConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Fix: Auto-reconnect if the connection is null or was closed!
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (Exception e) {
            connect(); // Fallback reconnect
        }
        return connection;
    }
}