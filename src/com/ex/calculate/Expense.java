package com.ex.calculate;

import java.io.Serializable;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id, year, month;
    private String category;
    private double amount;

    public Expense() {}

    public Expense(int year, int month, String category, double amount) {
        this.year = year; this.month = month;
        this.category = category; this.amount = amount;
    }

    public Expense(int id, int year, int month, String category, double amount) {
        this(year, month, category, amount);
        this.id = id;
    }

    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }
    public int    getYear()           { return year; }
    public void   setYear(int year)   { this.year = year; }
    public int    getMonth()          { return month; }
    public void   setMonth(int month) { this.month = month; }
    public String getCategory()               { return category; }
    public void   setCategory(String c)       { this.category = c; }
    public double getAmount()                 { return amount; }
    public void   setAmount(double amount)    { this.amount = amount; }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", year=" + year + ", month=" + month +
               ", category='" + category + "', amount=" + amount + '}';
    }
}
