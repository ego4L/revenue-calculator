package com.egoxide.finance.coreservice.service;


import com.egoxide.finance.coreservice.controller.DataController;
import com.egoxide.finance.coreservice.entity.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TransactionService {

    Iterable<Transaction> bulkSave(List<Transaction> transactions);

    Set<String> getSellingSymbols(int year);

    List<Transaction> getTransactionsBySymbol(String symbol, int year);

    Map<String, DataController.ProfitLoss> calculateAnnualRevenue(Set<String> sellingSymbolsForYear, int year);
}
