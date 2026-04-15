package com.ex.calculator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SecondaryController {
    private MoneyCalculatorBean bean;

    @FXML private Label averageExpensesLabel;

    public void setBean(MoneyCalculatorBean bean) {
        this.bean = bean;
        updateReport();
    }

    private void updateReport() {
        if (bean != null) {
            averageExpensesLabel.setText("Average Expenses: " +
                bean.computeAverageExpenses(bean.getExpenses().stream().mapToInt(Integer::intValue).toArray())
            );
        }
    }
}
