package chart;

import java.io.FileInputStream;
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

    private SQLConnection() {
        loadFromFile();   // 🔥 load latest saved settings
        connect();
    }

    private void loadFromFile() {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream("db.properties")) {
        props.load(fis);

        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        System.out.println("Using DB URL: " + url); // 🔥 debug

    } catch (Exception e) {
        System.out.println("⚠ Using default DB settings");

        // fallback (optional)
        this.url = "jdbc:oracle:thin:@localhost:1521:xe";
        this.user = "system";
        this.password = "password";
    }
}

    public static void reset() {
        instance = null;
    }

    private void connect() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            // Set a short login timeout (5 s) so a failed connection doesn't
            // block the caller for 20-30 seconds and freeze the UI.
            DriverManager.setLoginTimeout(5);
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

    public void resetConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null; // Forces the app to reconnect next time
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
