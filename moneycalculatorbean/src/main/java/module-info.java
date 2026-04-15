module com.ex.calculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.ex.calculator to javafx.fxml;
    exports com.ex.calculator;
}
