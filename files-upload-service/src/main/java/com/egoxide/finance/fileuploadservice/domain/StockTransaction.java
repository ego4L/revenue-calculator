package com.egoxide.finance.fileuploadservice.domain;

import java.time.LocalDateTime;

public class StockTransaction {

    private LocalDateTime dateTime;
    private String symbol;
    private PositiveInt price;
    private PositiveDouble quantity;
    private Action action;


    private StockTransaction() { }

    public static StockTransaction of(LocalDateTime dateAndTime, String symbol, String quantity, String price) {

        StockTransaction stockTransaction = new StockTransaction();

        stockTransaction.dateTime = dateAndTime;
        stockTransaction.symbol = symbol;
        double value = Double.parseDouble(quantity);
        stockTransaction.quantity = new PositiveDouble(value);

        int parsedPrise = Integer.parseInt(price);

        if (value == 0.0) {
            stockTransaction.action = Action.DIVIDENDS;
        } else {
            stockTransaction.action = parsedPrise > 0 ? Action.SELL : Action.BUY;
        }

        stockTransaction.price = new PositiveInt(Math.abs(parsedPrise));

        return stockTransaction;
    }

    public static StockTransaction of(LocalDateTime dateAndTime, String price) {

        StockTransaction stockTransaction = new StockTransaction();

        stockTransaction.dateTime = dateAndTime;
        stockTransaction.action = Integer.parseInt(price) >= 0 ? Action.TOPUP : Action.WITHDRAW;

        return stockTransaction;
    }
}
