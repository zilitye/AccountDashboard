import com.ex.calculate.MoneyCalculatorBean;

public class Main {
    public static void main(String[] args) {
        MoneyCalculatorBean bean = new MoneyCalculatorBean();
        bean.addIncome(100);
        bean.addIncome(200);
        bean.addExpense(50);
        bean.addExpense(30);

        System.out.println("Total Income: " + bean.getTotalIncome());
        System.out.println("Total Expenses: " + bean.getTotalExpenses());
    }
}
