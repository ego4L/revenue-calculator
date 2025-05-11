package com.egoxide.finance.coreservice.service;

import com.egoxide.finance.coreservice.controller.DataController;
import com.egoxide.finance.coreservice.domain.Action;
import com.egoxide.finance.coreservice.entity.Transaction;
import com.egoxide.finance.coreservice.repository.TransactionRepository;
import com.egoxide.finance.coreservice.util.CurrencyUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static java.math.BigDecimal.valueOf;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private static final Map<String, List<Double>> currencyRateMap = CurrencyUtil.getCurrencyRateForYear("eur", "usd");


    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Iterable<Transaction> bulkSave(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    @Override
    public Set<String> getSellingSymbols(int year) {

        List<String> sellTransactions = transactionRepository.findDistinctSymbolsByYearAndAction(year, Action.SELL);
        List<String> dividendTransactions = transactionRepository.findDistinctSymbolsByYearAndAction(year, Action.DIVIDENDS);
        sellTransactions.addAll(dividendTransactions);
        return new HashSet<>(sellTransactions);
    }

    @Override
    public List<Transaction> getTransactionsBySymbol(String symbol, int year) {
        LocalDateTime calculationYear = LocalDateTime.of(year + 1, Month.JANUARY, 1, 0, 0, 0);
        return transactionRepository.findAllChronologicallyBySymbol(symbol, calculationYear);
    }

    @Override
    public Map<String, DataController.ProfitLoss> calculateAnnualRevenue(Set<String> sellingSymbolsForYear, int year) {

        Map<String, DataController.ProfitLoss> tickerToRevenue = new HashMap<>();

        if (!currencyRateMap.containsKey(String.valueOf(year))) {
            throw new IllegalArgumentException("No currency data for the year: " + year);
        }

        for (String symbol : sellingSymbolsForYear) {
            List<Transaction> transactions = getTransactionsBySymbol(symbol, year);
            Queue<Transaction> buyTransactionsDeque = new ArrayDeque<>();

            DataController.ProfitLoss profitLoss = new DataController.ProfitLoss(0, 0);

            for (Transaction transaction : transactions) {

                int transactionYear = transaction.getDateTime().getYear();
                int transactionMonth = transaction.getDateTime().getMonthValue();

                switch (transaction.getAction()) {
                    case BUY -> buyTransactionsDeque.add(transaction);
                    case SELL -> {
                        double currencyRateEurUsd = getRate(transactionYear, transactionMonth);
                        int revenueEur = (int) (transaction.getPriceUsd() / currencyRateEurUsd);
                        BigDecimal soldAmount = valueOf(transaction.getQuantity());
                        do {

                            Transaction buyTransaction = buyTransactionsDeque.peek();
                            int buyYear = Objects.requireNonNull(buyTransaction).getDateTime().getYear();
                            int buyMonth = Objects.requireNonNull(buyTransaction).getDateTime().getMonthValue();

                            if (soldAmount.compareTo(valueOf(Objects.requireNonNull(buyTransaction.getQuantity()))) >= 0) {
                                revenueEur -= (int) (buyTransaction.getPriceUsd() / getRate(buyYear, buyMonth));
                                buyTransactionsDeque.poll();
                            } else {
                                BigDecimal buyingPricePerShare = valueOf(Objects.requireNonNull(buyTransaction).getPriceUsd() / buyTransaction.getQuantity());

                                double leftAfterSellingQuantity = valueOf(buyTransaction.getQuantity()).subtract(soldAmount).doubleValue();
                                buyTransaction.setQuantity(leftAfterSellingQuantity);
                                buyTransaction.setPriceUsd(buyingPricePerShare.multiply(BigDecimal.valueOf(leftAfterSellingQuantity)).intValue());
                                revenueEur -= (int) (soldAmount.multiply(buyingPricePerShare).doubleValue() / getRate(transactionYear, buyMonth));
                            }

                            soldAmount = soldAmount.subtract(BigDecimal.valueOf(buyTransaction.getQuantity()));

                        } while (soldAmount.compareTo(BigDecimal.valueOf(0)) > 0);

                        if (transactionYear == year) {
                            profitLoss = profitLoss.addRevenue(revenueEur);
                        }
                    }
                    case DIVIDENDS -> {

                        if (transactionYear != year) {
                            continue;
                        }

                        double monthCurrencyRate = getRate(transactionYear, transactionMonth);
                        int exchangedRevenue = (int) (transaction.getPriceUsd() / monthCurrencyRate);

                        profitLoss = profitLoss.addRevenue(exchangedRevenue);
                    }
                }
            }
            tickerToRevenue.put(symbol, profitLoss);
        }

        return tickerToRevenue;
    }

    private static Double getRate(int transactionYear, int buyMonth) {
        return currencyRateMap.get(String.valueOf(transactionYear)).get(buyMonth - 1);
    }
}
