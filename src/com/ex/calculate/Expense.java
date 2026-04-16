package com.ex.calculate;

import java.io.Serializable;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;         // optional primary key from DB
    private int year;
    private int month;
    private String category;
    private double amount;

    // Constructors
    public Expense() {}

    public Expense(int year, int month, String category, double amount) {
        this.year = year;
        this.month = month;
        this.category = category;
        this.amount = amount;
    }

    public Expense(int id, int year, int month, String category, double amount) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.category = category;
        this.amount = amount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Convenience method for debugging
    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}
