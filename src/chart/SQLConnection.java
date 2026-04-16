package chart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private static SQLConnection instance;
    private Connection connection;

    private final String url = "jdbc:oracle:thin:@LAPTOP-0DI29GMV:1521:xe"; // adjust if using service name
    private final String user = "system";
    private final String password = "password";

    private SQLConnection() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
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

    public static SQLConnection getInstance() {
        if (instance == null) {
            instance = new SQLConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnecting to database...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection state.");
            e.printStackTrace();
            connect();
        }
        return connection;
    }
}
