package com.ex.calculator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PrimaryController {
    private MoneyCalculatorBean bean = new MoneyCalculatorBean();

    @FXML private TextField incomeField;
    @FXML private TextField expenseField;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;

    @FXML
    private void addIncome() {
        int value = Integer.parseInt(incomeField.getText());
        bean.addIncome(value);
        updateTotals();
    }

    @FXML
    private void addExpense() {
        int value = Integer.parseInt(expenseField.getText());
        bean.addExpense(value);
        updateTotals();
    }

    private void updateTotals() {
        totalIncomeLabel.setText("Total Income: " + bean.computeTotalIncome(
            bean.getIncomes().stream().mapToInt(Integer::intValue).toArray()
        ));
        totalExpenseLabel.setText("Total Expenses: " + bean.computeTotalExpenses(
            bean.getExpenses().stream().mapToInt(Integer::intValue).toArray()
        ));
    }

    // Open secondary window
    @FXML
    private void openReportWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("secondary.fxml"));
        Parent root = loader.load();

        SecondaryController secController = loader.getController();
        secController.setBean(bean); // share the same bean

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Expense Report - Secondary");
        stage.show();
    }
}
