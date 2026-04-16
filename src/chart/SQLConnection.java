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
            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.OracleDriver");

            // Use SID = xe (or Service Name = xepdb1 if needed)
            String url = "jdbc:oracle:thin:@LAPTOP-0DI29GMV:1521:xe";
            String user = "system";
            String password = "password";

            // Establish connection
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found.");
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
