package com.egoxide.finance.coreservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransactionId implements Serializable {

    private LocalDateTime dateTime;
    private String symbol;
    private Integer price;

    public TransactionId() {
    }

    public TransactionId(LocalDateTime dateTime, String symbol, Integer price) {
        this.dateTime = dateTime;
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
        return Objects.equals(dateTime, that.dateTime) && Objects.equals(symbol, that.symbol) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, symbol, price);
    }
}
