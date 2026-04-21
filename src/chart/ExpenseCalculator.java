package chart;

import java.util.Arrays;
import java.util.List;

/**
 * ExpenseCalculator - Utility class for calculating expense metrics
 * Provides methods for yearly totals, monthly totals, averages, and comparisons
 */
public class ExpenseCalculator {

    /**
     * 1. Calculate Yearly Total Expenses
     * Sums all expenses for the year
     * @param expenses array of integers representing expenses
     * @return integer sum of all expenses
     * Example: [100, 200, 300] → 600
     */
    public static int calculateYearlyTotalExpenses(int[] expenses) {
        if (expenses == null || expenses.length == 0) {
            return 0;
        }
        int total = 0;
        for (int expense : expenses) {
            total += expense;
        }
        return total;
    }

    /**
     * Overloaded version for List<Integer>
     */
    public static int calculateYearlyTotalExpenses(List<Integer> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return 0;
        }
        return expenses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Overloaded version for double array
     */
    public static double calculateYearlyTotalExpensesDouble(double[] expenses) {
        if (expenses == null || expenses.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        for (double expense : expenses) {
            total += expense;
        }
        return total;
    }

    /**
     * 2. Calculate Monthly Total Expenses
     * Total expenses for a specific month
     * @param monthlyExpenses array of integers for a specific month
     * @return integer sum of monthly expenses
     * Example: [100, 200, 300] → 600
     */
    public static int calculateMonthlyTotalExpenses(int[] monthlyExpenses) {
        if (monthlyExpenses == null || monthlyExpenses.length == 0) {
            return 0;
        }
        int total = 0;
        for (int expense : monthlyExpenses) {
            total += expense;
        }
        return total;
    }

    /**
     * Overloaded version for List<Integer>
     */
    public static int calculateMonthlyTotalExpenses(List<Integer> monthlyExpenses) {
        if (monthlyExpenses == null || monthlyExpenses.isEmpty()) {
            return 0;
        }
        return monthlyExpenses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 3. Calculate Yearly Total by Category
     * Total expenses for a specific category in the year
     * @param categoryExpenses array of integers for a specific category
     * @return integer sum of category expenses
     * Example: [100, 200, 300] → 600
     */
    public static int calculateYearlyTotalByCategory(int[] categoryExpenses) {
        if (categoryExpenses == null || categoryExpenses.length == 0) {
            return 0;
        }
        int total = 0;
        for (int expense : categoryExpenses) {
            total += expense;
        }
        return total;
    }

    /**
     * Overloaded version for List<Integer>
     */
    public static int calculateYearlyTotalByCategory(List<Integer> categoryExpenses) {
        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            return 0;
        }
        return categoryExpenses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 4. Calculate Average Monthly Expenses
     * Average expenses per month (yearly total / number of months)
     * @param yearlyTotal total expenses for the year (integer)
     * @param numberOfMonths number of months (integer)
     * @return double average monthly expense
     * Example: (1000, 4) → 250.0
     */
    public static double calculateAverageMonthlyExpenses(int yearlyTotal, int numberOfMonths) {
        if (numberOfMonths <= 0) {
            return 0.0;
        }
        return (double) yearlyTotal / numberOfMonths;
    }

    /**
     * Overloaded version for double
     */
    public static double calculateAverageMonthlyExpenses(double yearlyTotal, int numberOfMonths) {
        if (numberOfMonths <= 0) {
            return 0.0;
        }
        return yearlyTotal / numberOfMonths;
    }

    /**
     * 5. Month-to-Month Comparison
     * Difference between two months (positive = increase, negative = decrease)
     * @param currentMonthTotal expenses for current month
     * @param previousMonthTotal expenses for previous month
     * @return integer difference (can be positive or negative)
     * Example: (Feb=300, Jan=100) → 200 (increase of 200)
     *          (Feb=50, Jan=100) → -50 (decrease of 50)
     */
    public static int monthToMonthComparison(int currentMonthTotal, int previousMonthTotal) {
        return currentMonthTotal - previousMonthTotal;
    }

    /**
     * Overloaded version for double
     */
    public static double monthToMonthComparison(double currentMonthTotal, double previousMonthTotal) {
        return currentMonthTotal - previousMonthTotal;
    }

    /**
     * Helper method to determine if it's an increase or decrease
     * @param difference the difference from monthToMonthComparison
     * @return string indicating "Increase" or "Decrease"
     */
    public static String getComparisonTrend(int difference) {
        return difference >= 0 ? "Increase" : "Decrease";
    }

    /**
     * 6. Calculate Percentage Change Between Months
     * Percentage change formula: ((Feb – Jan) / Jan) × 100
     * @param currentMonthTotal expenses for current month (February)
     * @param previousMonthTotal expenses for previous month (January)
     * @return double percentage change
     * Example: (Feb=150, Jan=100) → 50.0 (50% increase)
     *          (Feb=50, Jan=100) → -50.0 (50% decrease)
     */
    public static double calculatePercentageChange(int currentMonthTotal, int previousMonthTotal) {
        if (previousMonthTotal == 0) {
            return 0.0; // Cannot calculate percentage change if previous month is 0
        }
        return ((double) (currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100.0;
    }

    /**
     * Overloaded version for double
     */
    public static double calculatePercentageChange(double currentMonthTotal, double previousMonthTotal) {
        if (previousMonthTotal == 0) {
            return 0.0; // Cannot calculate percentage change if previous month is 0
        }
        return ((currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100.0;
    }

    /**
     * Helper method to format percentage change with symbol
     * @param percentageChange the percentage value
     * @return formatted string with + or - symbol
     */
    public static String formatPercentageChange(double percentageChange) {
        String symbol = percentageChange >= 0 ? "+" : "";
        return String.format("%s%.2f%%", symbol, percentageChange);
    }

    /**
     * Helper method to get color indicator for percentage change
     * @param percentageChange the percentage value
     * @return "green" for positive, "red" for negative, "gray" for zero
     */
    public static String getPercentageChangeColor(double percentageChange) {
        if (percentageChange > 0) {
            return "red"; // Increase in expenses is negative
        } else if (percentageChange < 0) {
            return "green"; // Decrease in expenses is positive
        } else {
            return "gray"; // No change
        }
    }
}
