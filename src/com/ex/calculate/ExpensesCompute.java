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
        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO expenses(year, month, category, amount) VALUES(?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
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
        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            String sql = "SELECT SUM(amount) FROM expenses WHERE year=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // Monthly total
    public double getMonthlyTotal(int year, int month) {
        double total = 0;
        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            String sql = "SELECT SUM(amount) FROM expenses WHERE year=? AND month=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // Totals by category (works for yearly or monthly)
    public Map<String, Double> getTotalsByCategory(int year, Integer month) {
        Map<String, Double> totals = new HashMap<>();
        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            String sql = "SELECT category, SUM(amount) FROM expenses WHERE year=?"
                       + (month != null ? " AND month=?" : "")
                       + " GROUP BY category";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, year);
            if (month != null) ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                totals.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMonthlyTotals'");
    }
}
