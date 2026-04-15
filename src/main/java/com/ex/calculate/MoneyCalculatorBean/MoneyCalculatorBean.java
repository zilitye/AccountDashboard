package com.ex.calculate.MoneyCalculatorBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MoneyCalculatorBean
 * A non-visual JavaBean for managing financial records (income and expenses).
 * Supports CRUD operations, computations, and persistence.
 */
public class MoneyCalculatorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Integer> incomes;
    private List<Integer> expenses;

    // No-argument constructor
    public MoneyCalculatorBean() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    // --- CRUD Operations ---
    public void addIncome(int amount) {
        incomes.add(amount);
    }

    public void addExpense(int amount) {
        expenses.add(amount);
    }

    public List<Integer> getIncomes() {
        return new ArrayList<>(incomes);
    }

    public List<Integer> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public void updateIncome(int index, int newAmount) {
        if (index >= 0 && index < incomes.size()) {
            incomes.set(index, newAmount);
        }
    }

    public void updateExpense(int index, int newAmount) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, newAmount);
        }
    }

    public void deleteIncome(int index) {
        if (index >= 0 && index < incomes.size()) {
            incomes.remove(index);
        }
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    // --- Required Methods ---
    public int computeTotalIncome(int[] incomeArray) {
        int total = 0;
        for (int val : incomeArray) {
            total += val;
        }
        return total;
    }

    public int computeTotalExpenses(int[] expenseArray) {
        int total = 0;
        for (int val : expenseArray) {
            total += val;
        }
        return total;
    }

    public int computeAverageExpenses() {
        if (expenses.isEmpty()) return 0;
        int total = 0;
        for (int val : expenses) {
            total += val;
        }
        return total / expenses.size();
    }

    // --- Utility Method to Add Expense to Array ---
    public int[] addExpenseRecord(int[] existingExpenses, int newExpense) {
        int[] updated = new int[existingExpenses.length + 1];
        System.arraycopy(existingExpenses, 0, updated, 0, existingExpenses.length);
        updated[existingExpenses.length] = newExpense;
        return updated;
    }
}
