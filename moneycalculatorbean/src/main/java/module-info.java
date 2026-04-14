module com.ex.calculator {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ex.calculator to javafx.fxml;
    exports com.ex.calculator;
}
