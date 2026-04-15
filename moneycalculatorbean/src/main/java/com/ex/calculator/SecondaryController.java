package com.ex.calculator;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

public class SecondaryController {
    private MoneyCalculatorBean bean;

    @FXML private Label averageExpensesLabel;
    @FXML private PieChart reportPieChart;

    public void setBean(MoneyCalculatorBean bean) {
        this.bean = bean;
        updateReport();
    }

    private void updateReport() {
        if (bean != null) {
            // Update average expenses
            averageExpensesLabel.setText("Average Expenses: " +
                bean.computeAverageExpenses(bean.getExpenses().stream().mapToInt(Integer::intValue).toArray())
            );

            // Update pie chart
            double totalIncome = bean.computeTotalIncome(
                bean.getIncomes().stream().mapToInt(Integer::intValue).toArray()
            );
            double totalExpense = bean.computeTotalExpenses(
                bean.getExpenses().stream().mapToInt(Integer::intValue).toArray()
            );

            reportPieChart.getData().clear();
            reportPieChart.getData().add(new PieChart.Data("Income", totalIncome));
            reportPieChart.getData().add(new PieChart.Data("Expenses", totalExpense));
        }
    }
}
