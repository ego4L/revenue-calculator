package com.egoxide.finance.coreservice.service;


import com.egoxide.finance.coreservice.entity.Transaction;

import java.util.List;
import java.util.Set;

public interface TransactionService {

    Iterable<Transaction> bulkSave(List<Transaction> transactions);

    Set<String> getSellingSymbolsForYear(int year);

    List<Transaction> getTransactionsBySymbolBeforeYearInclusive(String symbol, int year);

    List<Transaction> getTransactionsBySymbolChronologicallyTillYearInclusive(String symbol, int year);

    Double calculateAnnualRevenue(Set<String> sellingSymbolsForYear, int year);
}
