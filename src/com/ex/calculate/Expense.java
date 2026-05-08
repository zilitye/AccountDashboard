package com.ex.calculate; // Package name for expense calculation module

// Import Serializable to allow object saving/transfer
import java.io.Serializable;

/**
 * Expense model class
 * Represents a single expense record
 */
public class Expense implements Serializable {

    // Version ID for serialization compatibility
    private static final long serialVersionUID = 1L;

    // Expense fields
    private int id;          // Unique ID
    private int year;        // Expense year
    private int month;       // Expense month
    private String category;  // Expense category (e.g. Food, Transport)
    private double amount;   // Expense amount

    /**
     * Default constructor
     */
    public Expense() {}

    /**
     * Constructor without ID (used before saving to database)
     */
    public Expense(int year, int month, String category, double amount) {

        this.year = year;          // Set year
        this.month = month;        // Set month
        this.category = category;  // Set category
        this.amount = amount;      // Set amount
    }

    /**
     * Constructor with ID (used when loading from database)
     */
    public Expense(int id, int year, int month, String category, double amount) {

        // Reuse constructor to avoid duplicate code
        this(year, month, category, amount);

        // Set ID separately
        this.id = id;
    }

    // ===== GETTERS AND SETTERS =====

    public int getId() {
        return id; // Return expense ID
    }

    public void setId(int id) {
        this.id = id; // Set expense ID
    }

    public int getYear() {
        return year; // Return year
    }

    public void setYear(int year) {
        this.year = year; // Set year
    }

    public int getMonth() {
        return month; // Return month
    }

    public void setMonth(int month) {
        this.month = month; // Set month
    }

    public String getCategory() {
        return category; // Return category
    }

    public void setCategory(String c) {
        this.category = c; // Set category
    }

    public double getAmount() {
        return amount; // Return amount
    }

    public void setAmount(double amount) {
        this.amount = amount; // Set amount
    }

    /**
     * Convert object to readable string format
     */
    @Override
    public String toString() {

        // Return formatted expense details
        return "Expense{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}