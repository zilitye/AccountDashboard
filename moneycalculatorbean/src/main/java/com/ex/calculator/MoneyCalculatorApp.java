package com.ex.calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MoneyCalculatorApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load primary UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Money Calculator - Primary");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
