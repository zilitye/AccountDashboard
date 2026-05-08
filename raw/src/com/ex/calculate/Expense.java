package com.ex.calculate;

import java.io.Serializable;

public class Expense implements Serializable {
    public int id, year, month;
    public String category;
    public double amount;

    // Single simple constructor
    public Expense(int year, int month, String category, double amount) {
        this.year = year;
        this.month = month;
        this.category = category;
        this.amount = amount;
    }
}