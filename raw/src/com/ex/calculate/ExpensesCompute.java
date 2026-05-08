package com.ex.calculate;

import java.io.Serializable;

public class ExpensesCompute implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. Yearly Total Expenses -> Sum of all expenses for the year
    public int getYearlyTotalExpenses(int[] expenses) {
        int total = 0;
        if (expenses != null) {
            for (int expense : expenses) {
                total += expense;
            }
        }
        return total;
    }

    // 2. Monthly Total Expenses -> Total expenses for a specific month
    public int getMonthlyTotalExpenses(int[] expenses) {
        int total = 0;
        if (expenses != null) {
            for (int expense : expenses) {
                total += expense;
            }
        }
        return total;
    }

    // 3. Yearly Total by Category
    public int getYearlyTotalByCategory(int[] expenses) {
        int total = 0;
        if (expenses != null) {
            for (int expense : expenses) {
                total += expense;
            }
        }
        return total;
    }

    // 4. Average Monthly Expenses
    public double getAverageMonthlyExpenses(int totalYearly, int numMonths) {
        if (numMonths == 0) return 0.0;
        return (double) totalYearly / numMonths;
    }

    // 5. Month-to-Month Comparison (Difference)
    public int getMonthToMonthComparison(int month1Total, int month2Total) {
        // Difference between the two months.
        // Positive result = Increase, Negative result = Decrease
        return month2Total - month1Total; 
    }

    // 6. Percentage Change Between Months
    public double getPercentageChangeBetweenMonths(int month1Total, int month2Total) {
        if (month1Total == 0) return 0.0; // Prevent division by zero
        return ((double) (month2Total - month1Total) / month1Total) * 100.0;
    }
}