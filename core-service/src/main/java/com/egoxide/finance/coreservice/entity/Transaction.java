package com.egoxide.finance.coreservice.entity;


import com.egoxide.finance.coreservice.domain.Action;
import com.egoxide.finance.coreservice.domain.StockTransaction;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@IdClass(TransactionId.class)
public class Transaction {
    @Id
    private LocalDateTime dateTime;
    @Id
    private String symbol;
    @Id
    private Integer price;
    private Double quantity;
    private Action action;

    public Transaction() {
    }

    public Transaction(LocalDateTime dateTime, String symbol, Integer price, Double quantity, Action action) {
        this.dateTime = dateTime;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.action = action;
    }

    public static List<Transaction> convertFromAccountTransaction(List<StockTransaction> stockTransactions) {

        List<Transaction> transactions = new ArrayList<>();

        for (StockTransaction stockTransaction : stockTransactions) {

            transactions.add(new Transaction(stockTransaction.dateTime(),
                    stockTransaction.symbol(), stockTransaction.price().value(),
                    stockTransaction.quantity().value(), stockTransaction.action()));
        }

        return transactions;

    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                ", dateTime=" + dateTime +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", action=" + action +
                '}';
    }
}

