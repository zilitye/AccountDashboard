package chart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private static SQLConnection instance;
    private Connection connection;

    private static final String URL      = "jdbc:mysql://localhost:3306/accountdb";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private SQLConnection() { connect(); }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(5);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected.");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed.");
            e.printStackTrace();
        }
    }

    public static SQLConnection getInstance() {
        if (instance == null) instance = new SQLConnection();
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }
}
