package com.ex.calculator;

import java.io.*;
import java.util.ArrayList;

public class MoneyCalculatorBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Integer> incomes;
    private ArrayList<Integer> expenses;

    // No-argument constructor
    public MoneyCalculatorBean() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    // CRUD-like operations
    public void addIncome(int value) { incomes.add(value); }
    public void addExpense(int value) { expenses.add(value); }
    public ArrayList<Integer> getIncomes() { return incomes; }
    public ArrayList<Integer> getExpenses() { return expenses; }

    // Required methods
    public int computeTotalIncome(int[] values) {
        int sum = 0;
        for (int v : values) sum += v;
        return sum;
    }

    public int computeTotalExpenses(int[] values) {
        int sum = 0;
        for (int v : values) sum += v;
        return sum;
    }

    public double computeAverageExpenses(int[] values) {
        if (values.length == 0) return 0;
        return (double) computeTotalExpenses(values) / values.length;
    }

    // Persistence
    public void saveData(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static MoneyCalculatorBean loadData(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (MoneyCalculatorBean) ois.readObject();
        }
    }
}
