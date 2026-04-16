package chart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    // Single instance of SQLConnection
    private static SQLConnection instance;
    private Connection connection;

    // Private constructor prevents direct instantiation
    private SQLConnection() {
        try {
            // Adjust DB name, user, and password to your setup
            String url = "jdbc:mysql://localhost:3306/accountdb";
            String user = "root";
            String password = "password";

            // Load MySQL JDBC driver (optional in newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }

    // Public method to get the single instance
    public static SQLConnection getInstance() {
        if (instance == null) {
            instance = new SQLConnection();
        }
        return instance;
    }

    // Getter for the connection object
    public Connection getConnection() {
        return connection;
    }
}
