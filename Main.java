import java.util.Scanner;

import src.com.ex.calculate.model.MoneyCalculatorBean;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MoneyCalculatorBean bean = new MoneyCalculatorBean();

        System.out.print("Enter number of incomes: ");
        int incomeCount = sc.nextInt();
        for (int i = 0; i < incomeCount; i++) {
            System.out.print("Enter income " + (i + 1) + ": ");
            bean.addIncome(sc.nextInt());
        }

        System.out.print("Enter number of expenses: ");
        int expenseCount = sc.nextInt();
        for (int i = 0; i < expenseCount; i++) {
            System.out.print("Enter expense " + (i + 1) + ": ");
            bean.addExpense(sc.nextInt());
        }

        System.out.println("Total Income: " + bean.getTotalIncome());
        System.out.println("Total Expenses: " + bean.getTotalExpenses());

        sc.close();
    }
}
