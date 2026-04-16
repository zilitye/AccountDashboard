package com.ex.calculate;

public class Expense {
    private int year;
    private int month;
    private String category;
    private double amount;

    public Expense(int year, int month, String category, double amount) {
        this.year = year;
        this.month = month;
        this.category = category;
        this.amount = amount;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
}
