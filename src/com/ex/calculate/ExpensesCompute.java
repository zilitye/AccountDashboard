package com.ex.calculate; // Package for expense calculation logic

// Import database connection helper
import chart.SQLConnection;

// Serializable allows object saving/transferring
import java.io.Serializable;

// SQL classes for database operations
import java.sql.*;

// Utility collections
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for computing and retrieving expense data
 * from the database
 */
public class ExpensesCompute implements Serializable {

    // Version ID for serialization
    private static final long serialVersionUID = 1L;

    /**
     * Get database connection from singleton class
     */
    private Connection conn() {
        return SQLConnection.getInstance().getConnection();
    }

    /**
     * Insert new expense record into database
     */
    public void addExpense(int year, int month,
                           String category, double amount) {

        // Get connection
        Connection conn = conn();

        // Stop if connection is not available
        if (conn == null) return;

        try (
                // Prepare SQL insert statement
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO expenses(year,month,category,amount) VALUES(?,?,?,?)"
                )
        ) {

            // Set year value
            ps.setInt(1, year);

            // Set month value
            ps.setInt(2, month);

            // Set category value
            ps.setString(3, category);

            // Set amount value
            ps.setDouble(4, amount);

            // Execute insert query
            ps.executeUpdate();

        } catch (SQLException e) {

            // Print SQL error
            e.printStackTrace();
        }
    }

    /**
     * Get total expenses for a year
     */
    public double getYearlyTotal(int year) {

        // Get connection
        Connection conn = conn();

        // Return 0 if no connection
        if (conn == null) return 0;

        try (
                // SQL query for yearly sum
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT SUM(amount) FROM expenses WHERE year=?"
                )
        ) {

            // Set year parameter
            ps.setInt(1, year);

            try (
                    // Execute query
                    ResultSet rs = ps.executeQuery()
            ) {

                // Return sum if exists
                if (rs.next()) return rs.getDouble(1);
            }

        } catch (SQLException e) {

            // Print error
            e.printStackTrace();
        }

        // Default return
        return 0;
    }

    /**
     * Get total for a specific month
     */
    public double getMonthlyTotal(int year, int month) {

        // Loop through all monthly totals
        for (Map.Entry<Integer, Double> e
                : getMonthlyTotals(year).entrySet()) {

            // If month matches, return value
            if (e.getKey() == month)
                return e.getValue();
        }

        // If not found return 0
        return 0;
    }

    /**
     * Get total expenses grouped by category
     */
    public Map<String, Double> getTotalsByCategory(
            int year, Integer month) {

        // Map to store results
        Map<String, Double> totals = new HashMap<>();

        // Get connection
        Connection conn = conn();

        // Return empty map if no connection
        if (conn == null) return totals;

        // SQL depends on whether month filter exists
        String sql = (month != null)
                ? "SELECT category, month, SUM(amount) " +
                  "FROM expenses WHERE year=? GROUP BY category, month"
                : "SELECT category, SUM(amount) " +
                  "FROM expenses WHERE year=? GROUP BY category";

        try (
                // Prepare SQL
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            // Set year parameter
            ps.setInt(1, year);

            try (
                    // Execute query
                    ResultSet rs = ps.executeQuery()
            ) {

                // Loop results
                while (rs.next()) {

                    // If filtering by month
                    if (month != null) {

                        // Check correct month
                        if (toMonthInt(rs, 2) == month) {

                            // Add category total
                            totals.put(
                                    rs.getString(1),
                                    rs.getDouble(3)
                            );
                        }

                    } else {

                        // Add category total without month filter
                        totals.put(
                                rs.getString(1),
                                rs.getDouble(2)
                        );
                    }
                }
            }

        } catch (SQLException e) {

            // Print error
            e.printStackTrace();
        }

        // Return result map
        return totals;
    }

    /**
     * Get average monthly expense
     */
    public double getAverageMonthlyExpenses(int year) {

        // Yearly total divided by 12 months
        return getYearlyTotal(year) / 12.0;
    }

    /**
     * Compare two months (percentage difference)
     */
    public double getMonthComparison(int year,
                                     int month1,
                                     int month2) {

        // Get first month total
        double t1 = getMonthlyTotal(year, month1);

        // Avoid division by zero
        if (t1 == 0) return 0;

        // Calculate percentage difference
        return ((getMonthlyTotal(year, month2) - t1)
                / t1) * 100.0;
    }

    /**
     * Get all monthly totals for a year
     */
    public Map<Integer, Double> getMonthlyTotals(int year) {

        // Map to store month -> total
        Map<Integer, Double> totals = new HashMap<>();

        // Get connection
        Connection conn = conn();

        // Return empty map if no connection
        if (conn == null) return totals;

        try (
                // SQL for monthly sum
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT month, SUM(amount) " +
                        "FROM expenses WHERE year=? " +
                        "GROUP BY month ORDER BY month"
                )
        ) {

            // Set year parameter
            ps.setInt(1, year);

            try (
                    // Execute query
                    ResultSet rs = ps.executeQuery()
            ) {

                // Loop results
                while (rs.next()) {

                    // Convert month and store value
                    totals.put(
                            toMonthInt(rs, 1),
                            rs.getDouble(2)
                    );
                }
            }

        } catch (SQLException e) {

            // Print error
            e.printStackTrace();
        }

        // Return map
        return totals;
    }

    /**
     * Convert month column (int or string) to integer
     */
    private int toMonthInt(ResultSet rs, int col)
            throws SQLException {

        try {

            // Try reading as integer
            return rs.getInt(col);

        } catch (Exception e) {

            // If fails, convert from string
            return monthNameToInt(rs.getString(col));
        }
    }

    /**
     * Convert month name to integer (1–12)
     */
    private int monthNameToInt(String m) {

        // Convert to lowercase for matching
        switch (m.toLowerCase()) {

            case "january": case "jan": return 1;
            case "february": case "feb": return 2;
            case "march": case "mar": return 3;
            case "april": case "apr": return 4;
            case "may": return 5;
            case "june": case "jun": return 6;
            case "july": case "jul": return 7;
            case "august": case "aug": return 8;
            case "september": case "sep": return 9;
            case "october": case "oct": return 10;
            case "november": case "nov": return 11;
            case "december": case "dec": return 12;

            // If numeric string
            default:
                return Integer.parseInt(m);
        }
    }
}