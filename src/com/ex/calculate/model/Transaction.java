package src.com.ex.calculate.model;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private double amount;
    private String currency;
    private String type;          // e.g. "deposit", "withdrawal"
    private LocalDateTime date;   // timestamp of transaction

    // Constructors
    public Transaction() {}

    public Transaction(int id, double amount, String currency, String type, LocalDateTime date) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.date = date;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    // Utility methods
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                '}';
    }
}
