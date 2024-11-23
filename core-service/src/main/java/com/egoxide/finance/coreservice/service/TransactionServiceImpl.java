package com.egoxide.finance.coreservice.service;

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

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Iterable<Transaction> bulkSave(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    @Override
    public Set<String> getSellingSymbolsForYear(int year) {

        List<String> sellTransactions = transactionRepository.findDistinctSymbolsByYearAndAction(year, Action.SELL);
        List<String> dividendTransactions = transactionRepository.findDistinctSymbolsByYearAndAction(year, Action.DIVIDENDS);
        sellTransactions.addAll(dividendTransactions);
        return new HashSet<>(sellTransactions);
    }

    @Override
    public List<Transaction> getTransactionsBySymbolBeforeYearInclusive(String symbol, int year) {
        LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59, 59);
        return transactionRepository.findAllChronologicallyBySymbolBeforeYearInclusive(symbol, endOfYear);
    }

    @Override
    public List<Transaction> getTransactionsBySymbolChronologicallyTillYearInclusive(String symbol, int year) {
        LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59, 59);
        return transactionRepository.findAllChronologicallyBySymbolBeforeYearInclusive(symbol, endOfYear);
    }


    @Override
    public Double calculateAnnualRevenue(Set<String> sellingSymbolsForYear, int year) {

        Map<Integer, Integer> result = new TreeMap<>();
        Map<String, List<Double>> currencyRateMap = CurrencyUtil.getCurrencyRateForYear("eur", "usd");

        if (!currencyRateMap.containsKey(String.valueOf(year))) {
            return 0.0;
        }

        for (String symbol : sellingSymbolsForYear) {
            List<Transaction> transactions = getTransactionsBySymbolChronologicallyTillYearInclusive(symbol, year);
            Queue<Transaction> deque = new ArrayDeque<>();

            for (Transaction transaction : transactions) {

                int transYear = transaction.getDateTime().getYear();
                int transMonth = transaction.getDateTime().getMonthValue();

                switch (transaction.getAction()) {
                    case BUY -> deque.add(transaction);
                    case SELL -> {
                        double monthCurrencyRate = currencyRateMap.get(String.valueOf(year)).get(transMonth - 1);
                        int exchangedRevenue = (int) (transaction.getPrice() / monthCurrencyRate);

                        BigDecimal amount = valueOf(transaction.getQuantity());

                        int purchasedStocks = 0;

                        do {

                            Transaction buyTransaction = deque.peek();
                            int buyMonth = Objects.requireNonNull(buyTransaction).getDateTime().getMonthValue();

                            if (amount.compareTo(valueOf(Objects.requireNonNull(buyTransaction.getQuantity()))) >= 0) {
                                purchasedStocks += (int) (buyTransaction.getPrice() / currencyRateMap.get(String.valueOf(transYear)).get(buyMonth - 1));
                                deque.poll();
                            } else {

                                buyTransaction.setQuantity(BigDecimal.valueOf(buyTransaction.getQuantity()).subtract(amount).doubleValue());
                                purchasedStocks += (int) (amount.doubleValue() / currencyRateMap.get(String.valueOf(transYear)).get(buyMonth - 1));
                            }

                            amount = amount.subtract(BigDecimal.valueOf(buyTransaction.getQuantity()));

                        } while (amount.compareTo(BigDecimal.valueOf(0)) > 0);

                        exchangedRevenue -= purchasedStocks;

                        result.merge(transYear, exchangedRevenue, Integer::sum);
                    }
                    case DIVIDENDS -> {

                        double monthCurrencyRate = currencyRateMap.get(String.valueOf(transYear)).get(transMonth - 1);
                        int exchangedRevenue = (int) (transaction.getPrice() / monthCurrencyRate);
                        result.merge(transYear, exchangedRevenue, Integer::sum);
                    }
                }
            }
        }

        return result.containsKey(year) ? result.get(year) / 100.0 : 0.0;
    }
}
