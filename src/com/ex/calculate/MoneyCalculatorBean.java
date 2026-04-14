package src.com.ex.calculate;

import java.io.Serializable;
import java.util.ArrayList;

public class MoneyCalculatorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // Internal storage for income and expenses
    private ArrayList<Integer> incomes;
    private ArrayList<Integer> expenses;

    // No-argument constructor
    public MoneyCalculatorBean() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    // --- CRUD Operations ---
    public void addIncome(int income) {
        incomes.add(income);
    }

    public void addExpense(int expense) {
        expenses.add(expense);
    }

    public ArrayList<Integer> getIncomes() {
        return incomes;
    }

    public ArrayList<Integer> getExpenses() {
        return expenses;
    }

    public void clearIncomes() {
        incomes.clear();
    }

    public void clearExpenses() {
        expenses.clear();
    }

    // --- Required Methods ---
    public int computeTotalIncome(int[] incomeArray) {
        int total = 0;
        for (int i : incomeArray) {
            total += i;
        }
        return total;
    }

    public int computeTotalExpenses(int[] expenseArray) {
        int total = 0;
        for (int e : expenseArray) {
            total += e;
        }
        return total;
    }

    public int[] computeAverageExpenses(int[] expenseArray, int newExpense) {
        int[] updated = new int[expenseArray.length + 1];
        System.arraycopy(expenseArray, 0, updated, 0, expenseArray.length);
        updated[expenseArray.length] = newExpense;
        return updated;
    }

    // --- Utility Methods ---
    public int getTotalIncome() {
        return computeTotalIncome(incomes.stream().mapToInt(Integer::intValue).toArray());
    }

    public int getTotalExpenses() {
        return computeTotalExpenses(expenses.stream().mapToInt(Integer::intValue).toArray());
    }
}
