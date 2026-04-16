package com.ex.calculate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpensesCompute implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Expense> expenses;

    public ExpensesCompute() {
        expenses = new ArrayList<>();
    }

    public void addExpense(Expense e) {
        expenses.add(e);
    }

    public double getYearlyTotal(int year) {
        return expenses.stream()
                .filter(e -> e.getYear() == year)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getMonthlyTotal(int year, int month) {
        return expenses.stream()
                .filter(e -> e.getYear() == year && e.getMonth() == month)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getCategoryTotal(int year, String category) {
        return expenses.stream()
                .filter(e -> e.getYear() == year && e.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getAverageMonthlyExpenses(int year) {
        return getYearlyTotal(year) / 12.0;
    }

    public double compareMonths(int year, int month1, int month2) {
        return getMonthlyTotal(year, month2) - getMonthlyTotal(year, month1);
    }

    public double percentageChange(int year, int month1, int month2) {
        double first = getMonthlyTotal(year, month1);
        double second = getMonthlyTotal(year, month2);
        return (second - first) / first * 100.0;
    }
}
