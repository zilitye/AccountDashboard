package com.ex.calculate; 
// Defines the package name for organizing classes

import java.io.Serializable; 
// Imports Serializable so objects can be saved and loaded

public class Expense implements Serializable { 
// Expense class that can be serialized

    private static final long serialVersionUID = 1L; 
    // Version ID for serialization

    // ===== Variables =====
    
    private int id; 
    // Stores expense ID

    private int year; 
    // Stores year of expense

    private int month; 
    // Stores month of expense

    private String category; 
    // Stores expense category

    private double amount; 
    // Stores expense amount

    // ===== Default Constructor =====
    
    public Expense() {
        // Empty constructor
    }

    // ===== Constructor Without ID =====
    
    public Expense(int year, int month, String category, double amount) {

        this.year = year; 
        // Assign year

        this.month = month; 
        // Assign month

        this.category = category; 
        // Assign category

        this.amount = amount; 
        // Assign amount
    }

    // ===== Constructor With ID =====
    
    public Expense(int id, int year, int month, String category, double amount) {

        this.id = id; 
        // Assign ID

        this.year = year; 
        // Assign year

        this.month = month; 
        // Assign month

        this.category = category; 
        // Assign category

        this.amount = amount; 
        // Assign amount
    }

    // ===== Getter and Setter for ID =====
    
    public int getId() {
        return id; 
        // Return ID
    }

    public void setId(int id) {
        this.id = id; 
        // Update ID
    }

    // ===== Getter and Setter for Year =====
    
    public int getYear() {
        return year; 
        // Return year
    }

    public void setYear(int year) {
        this.year = year; 
        // Update year
    }

    // ===== Getter and Setter for Month =====
    
    public int getMonth() {
        return month; 
        // Return month
    }

    public void setMonth(int month) {
        this.month = month; 
        // Update month
    }

    // ===== Getter and Setter for Category =====
    
    public String getCategory() {
        return category; 
        // Return category
    }

    public void setCategory(String category) {
        this.category = category; 
        // Update category
    }

    // ===== Getter and Setter for Amount =====
    
    public double getAmount() {
        return amount; 
        // Return amount
    }

    public void setAmount(double amount) {
        this.amount = amount; 
        // Update amount
    }

    // ===== Convert Object to String =====
    
    @Override
    public String toString() {

        return "Expense{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                '}';
        // Returns object details as a string
    }
}