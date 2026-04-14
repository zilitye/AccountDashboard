
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MoneyCalculatorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Integer> incomes;
    private List<Integer> expenses;

    // No-argument constructor
    public MoneyCalculatorBean() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    // CRUD operations for income
    public void addIncome(int amount) {
        incomes.add(amount);
    }

    public void updateIncome(int index, int amount) {
        if (index >= 0 && index < incomes.size()) {
            incomes.set(index, amount);
        }
    }

    public int readIncome(int index) {
        return (index >= 0 && index < incomes.size()) ? incomes.get(index) : 0;
    }

    public void deleteIncome(int index) {
        if (index >= 0 && index < incomes.size()) {
            incomes.remove(index);
        }
    }

    // CRUD operations for expenses
    public void addExpense(int amount) {
        expenses.add(amount);
    }

    public void updateExpense(int index, int amount) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, amount);
        }
    }

    public int readExpense(int index) {
        return (index >= 0 && index < expenses.size()) ? expenses.get(index) : 0;
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    // Required methods
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

    // Utility: get all incomes/expenses
    public List<Integer> getIncomes() {
        return incomes;
    }

    public List<Integer> getExpenses() {
        return expenses;
    }
}
