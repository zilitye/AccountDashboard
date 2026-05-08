package com.ex.calculate;
// Package name

import chart.SQLConnection;
// Import database connection class

import java.io.Serializable;
// Allows object serialization

import java.sql.*;
// Import SQL classes

import java.util.HashMap;
import java.util.Map;
// Import Map and HashMap

public class ExpensesCompute implements Serializable {
    // Class for expense calculations

    private static final long serialVersionUID = 1L;
    // Serialization version ID

    // ===== Add Expense =====

    public void addExpense(int year, int month, String category, double amount) {

        Connection conn = SQLConnection.getInstance().getConnection();
        // Get database connection

        if (conn == null) {
            // Check if DB connection failed

            System.err.println("Database offline");
            // Print error message

            return;
            // Stop method
        }

        String sql =
                "INSERT INTO expenses(year, month, category, amount) VALUES(?,?,?,?)";
        // SQL insert query

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Create prepared statement

            ps.setInt(1, year);
            // Set year value

            ps.setInt(2, month);
            // Set month value

            ps.setString(3, category);
            // Set category value

            ps.setDouble(4, amount);
            // Set amount value

            ps.executeUpdate();
            // Execute insert query

        } catch (SQLException e) {

            e.printStackTrace();
            // Print SQL error
        }
    }

    // ===== Get Yearly Total =====

    public double getYearlyTotal(int year) {

        double total = 0;
        // Store total amount

        Connection conn = SQLConnection.getInstance().getConnection();
        // Get database connection

        if (conn == null)
            return 0;
        // Return 0 if DB offline

        String sql =
                "SELECT SUM(amount) FROM expenses WHERE year=?";
        // SQL query

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            // Set year

            try (ResultSet rs = ps.executeQuery()) {
                // Execute query

                if (rs.next())
                    total = rs.getDouble(1);
                // Get total amount
            }

        } catch (SQLException e) {

            e.printStackTrace();
            // Print SQL error
        }

        return total;
        // Return yearly total
    }

    // ===== Get Monthly Total =====

    public double getMonthlyTotal(int year, int month) {

        double total = 0;
        // Store monthly total

        Connection conn = SQLConnection.getInstance().getConnection();
        // Get DB connection

        if (conn == null)
            return 0;
        // Return 0 if offline

        String sql =
                "SELECT month, SUM(amount) FROM expenses WHERE year=? GROUP BY month";
        // SQL query

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            // Set year

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    // Loop through results

                    int dbMonth = rs.getInt(1);
                    // Get month from DB

                    if (dbMonth == month) {
                        // Check matching month

                        total = rs.getDouble(2);
                        // Get total amount

                        break;
                        // Stop loop
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
            // Print SQL error
        }

        return total;
        // Return monthly total
    }

    // ===== Totals By Category =====

    public Map<String, Double> getTotalsByCategory(int year, Integer month) {

        Map<String, Double> totals = new HashMap<>();
        // Store category totals

        Connection conn = SQLConnection.getInstance().getConnection();
        // Get DB connection

        if (conn == null)
            return totals;
        // Return empty map if offline

        String sql;
        // SQL query variable

        if (month != null) {

            sql =
                    "SELECT category, month, SUM(amount) FROM expenses WHERE year=? GROUP BY category, month";
            // Monthly category totals

        } else {

            sql =
                    "SELECT category, SUM(amount) FROM expenses WHERE year=? GROUP BY category";
            // Yearly category totals
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            // Set year

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String category = rs.getString(1);
                    // Get category name

                    if (month != null) {

                        int dbMonth = rs.getInt(2);
                        // Get DB month

                        if (dbMonth == month) {

                            totals.put(category, rs.getDouble(3));
                            // Save category total
                        }

                    } else {

                        totals.put(category, rs.getDouble(2));
                        // Save yearly category total
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
            // Print SQL error
        }

        return totals;
        // Return totals map
    }

    // ===== Average Monthly Expense =====

    public double getAverageMonthlyExpenses(int year) {

        return getYearlyTotal(year) / 12.0;
        // Calculate average monthly expense
    }

    // ===== Compare Two Months =====

    public double getMonthComparison(int year, int month1, int month2) {

        double total1 = getMonthlyTotal(year, month1);
        // Get first month total

        double total2 = getMonthlyTotal(year, month2);
        // Get second month total

        if (total1 == 0)
            return 0;
        // Avoid division by zero

        return ((total2 - total1) / total1) * 100.0;
        // Return percentage difference
    }

    // ===== Get All Monthly Totals =====

    public Map<Integer, Double> getMonthlyTotals(int year) {

        Map<Integer, Double> totals = new HashMap<>();
        // Store monthly totals

        Connection conn = SQLConnection.getInstance().getConnection();
        // Get DB connection

        if (conn == null)
            return totals;
        // Return empty map if offline

        String sql =
                "SELECT month, SUM(amount) FROM expenses WHERE year=? GROUP BY month ORDER BY month";
        // SQL query

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            // Set year

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    totals.put(rs.getInt(1), rs.getDouble(2));
                    // Store month and total
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
            // Print SQL error
        }

        return totals;
        // Return monthly totals
    }
}