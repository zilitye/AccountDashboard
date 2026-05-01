package com.ex.calculate;

import chart.SQLConnection;
import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ExpensesCompute implements Serializable {
    private static final long serialVersionUID = 1L;

    // Insert new expense into DB
    public void addExpense(int year, int month, String category, double amount) {
        // 1. Get connection OUTSIDE the try block
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) {
            System.err.println("[ExpensesCompute] Cannot add expense: DB offline");
            return;
        }

        String sql = "INSERT INTO expenses(year, month, category, amount) VALUES(?,?,?,?)";
        
        // 2. Only put the PreparedStatement inside the try block
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setString(3, category);
            ps.setDouble(4, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Yearly total
    public double getYearlyTotal(int year) {
        double total = 0;
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) return 0; // Return 0 if offline

        String sql = "SELECT SUM(amount) FROM expenses WHERE year=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // Monthly total
    public double getMonthlyTotal(int year, int month) {
        double total = 0;
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) return 0;

        // Query all months for the year, then filter in Java
        String sql = "SELECT month, SUM(amount) as monthly_sum FROM expenses WHERE year=? GROUP BY month";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            System.out.println("[ExpensesCompute] Executing: " + sql + " with year=" + year + " (will filter for month=" + month + " in Java)");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Handle both NUMBER and VARCHAR2 month columns
                    int dbMonth;
                    try {
                        dbMonth = rs.getInt(1);
                    } catch (Exception e) {
                        String monthStr = rs.getString(1);
                        dbMonth = convertMonthNameToNumber(monthStr);
                    }
                    
                    if (dbMonth == month) {
                        total = rs.getDouble(2);
                        System.out.println("[ExpensesCompute] Found month " + month + " with total: " + total);
                        break;
                    }
                }
            }
            System.out.println("[ExpensesCompute] Monthly total result for month " + month + ": " + total);
        } catch (SQLException e) {
            System.err.println("[ExpensesCompute] ERROR in getMonthlyTotal for year=" + year + ", month=" + month);
            e.printStackTrace();
        }
        return total;
    }

    // Totals by category (works for yearly or monthly)
    public Map<String, Double> getTotalsByCategory(int year, Integer month) {
        Map<String, Double> totals = new HashMap<>();
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) return totals; // Return empty map if offline

        String sql;
        if (month != null) {
            // Get totals for all months, will filter by specific month in Java
            sql = "SELECT category, month, SUM(amount) as category_sum FROM expenses WHERE year=? GROUP BY category, month ORDER BY month";
        } else {
            sql = "SELECT category, SUM(amount) as category_sum FROM expenses WHERE year=? GROUP BY category";
        }
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            
            if (month != null) {
                System.out.println("[ExpensesCompute] Executing: " + sql + " with year=" + year + " (will filter for month=" + month + " in Java)");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String category = rs.getString(1);
                        // Handle both NUMBER and VARCHAR2 month columns
                        int dbMonth;
                        try {
                            dbMonth = rs.getInt(2);
                        } catch (Exception e) {
                            String monthStr = rs.getString(2);
                            dbMonth = convertMonthNameToNumber(monthStr);
                        }
                        
                        if (dbMonth == month) {
                            double amount = rs.getDouble(3);
                            totals.put(category, amount);
                        }
                    }
                }
            } else {
                System.out.println("[ExpensesCompute] Executing: " + sql + " with year=" + year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totals.put(rs.getString(1), rs.getDouble(2));
                    }
                }
            }
            System.out.println("[ExpensesCompute] Category totals result: " + totals);
        } catch (SQLException e) {
            System.err.println("[ExpensesCompute] ERROR in getTotalsByCategory for year=" + year + ", month=" + month);
            e.printStackTrace();
        }
        return totals;
    }

    // Average monthly expenses
    public double getAverageMonthlyExpenses(int year) {
        return getYearlyTotal(year) / 12.0;
    }

    // Month-to-month comparison percentage
    public double getMonthComparison(int year, int month1, int month2) {
        double total1 = getMonthlyTotal(year, month1);
        double total2 = getMonthlyTotal(year, month2);
        if (total1 == 0) return 0;
        return ((total2 - total1) / total1) * 100.0;
    }

    public Map<Integer, Double> getMonthlyTotals(int year) {
        Map<Integer, Double> totals = new HashMap<>();
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) return totals;

        String sql = "SELECT month, SUM(amount) FROM expenses WHERE year=? GROUP BY month ORDER BY month";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Handle both NUMBER and VARCHAR2 month columns
                    int month;
                    try {
                        month = rs.getInt(1);
                    } catch (Exception e) {
                        // If it's VARCHAR2, convert month name to number
                        String monthStr = rs.getString(1);
                        month = convertMonthNameToNumber(monthStr);
                    }
                    totals.put(month, rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    private int convertMonthNameToNumber(String monthName) {
        switch (monthName.toLowerCase()) {
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
            default: return Integer.parseInt(monthName); // fallback for numeric strings
        }
    }
}