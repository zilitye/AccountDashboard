package com.ex.calculate;

import chart.SQLConnection;
import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ExpensesCompute implements Serializable {
    private static final long serialVersionUID = 1L;

    private Connection conn() { return SQLConnection.getInstance().getConnection(); }

    public void addExpense(int year, int month, String category, double amount) {
        Connection conn = conn(); if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO expenses(year,month,category,amount) VALUES(?,?,?,?)")) {
            ps.setInt(1, year); ps.setInt(2, month);
            ps.setString(3, category); ps.setDouble(4, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public double getYearlyTotal(int year) {
        Connection conn = conn(); if (conn == null) return 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SUM(amount) FROM expenses WHERE year=?")) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getMonthlyTotal(int year, int month) {
        for (Map.Entry<Integer, Double> e : getMonthlyTotals(year).entrySet())
            if (e.getKey() == month) return e.getValue();
        return 0;
    }

    public Map<String, Double> getTotalsByCategory(int year, Integer month) {
        Map<String, Double> totals = new HashMap<>();
        Connection conn = conn(); if (conn == null) return totals;

        String sql = month != null
            ? "SELECT category, month, SUM(amount) FROM expenses WHERE year=? GROUP BY category, month"
            : "SELECT category, SUM(amount) FROM expenses WHERE year=? GROUP BY category";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (month != null) {
                        if (toMonthInt(rs, 2) == month)
                            totals.put(rs.getString(1), rs.getDouble(3));
                    } else {
                        totals.put(rs.getString(1), rs.getDouble(2));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    public double getAverageMonthlyExpenses(int year) { return getYearlyTotal(year) / 12.0; }

    public double getMonthComparison(int year, int month1, int month2) {
        double t1 = getMonthlyTotal(year, month1);
        if (t1 == 0) return 0;
        return ((getMonthlyTotal(year, month2) - t1) / t1) * 100.0;
    }

    public Map<Integer, Double> getMonthlyTotals(int year) {
        Map<Integer, Double> totals = new HashMap<>();
        Connection conn = conn(); if (conn == null) return totals;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT month, SUM(amount) FROM expenses WHERE year=? GROUP BY month ORDER BY month")) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) totals.put(toMonthInt(rs, 1), rs.getDouble(2));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    // Reads a month column that may be INT or month-name VARCHAR
    private int toMonthInt(ResultSet rs, int col) throws SQLException {
        try { return rs.getInt(col); }
        catch (Exception e) { return monthNameToInt(rs.getString(col)); }
    }

    private int monthNameToInt(String m) {
        switch (m.toLowerCase()) {
            case "january":  case "jan": return 1;
            case "february": case "feb": return 2;
            case "march":    case "mar": return 3;
            case "april":    case "apr": return 4;
            case "may":                  return 5;
            case "june":     case "jun": return 6;
            case "july":     case "jul": return 7;
            case "august":   case "aug": return 8;
            case "september":case "sep": return 9;
            case "october":  case "oct": return 10;
            case "november": case "nov": return 11;
            case "december": case "dec": return 12;
            default: return Integer.parseInt(m);
        }
    }
}
