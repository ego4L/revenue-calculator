package com.egoxide.finance.coreservice.controller;


import com.egoxide.finance.coreservice.domain.Action;
import com.egoxide.finance.coreservice.domain.StockTransaction;
import com.egoxide.finance.coreservice.entity.Transaction;
import com.egoxide.finance.coreservice.message.ResponseMessage;
import com.egoxide.finance.coreservice.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
public class DataController {

    public record RevenueReport(Map<String, ProfitLoss> tickerToRevenue, Double gain, Double loss, Double total) {
    }

    public record ProfitLoss(int profit, int loss) {
        public ProfitLoss addRevenue(int revenue) {
            if (revenue > 0) {
                return new ProfitLoss(this.profit + revenue, this.loss);
            } else {
                return new ProfitLoss(this.profit, this.loss + Math.abs(revenue));
            }
        }
    }

    private final TransactionService transactionService;

    public DataController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private record AccountTransaction(LocalDateTime dateTime, int amount, Action action) {
    }

    public record ConsolidateActionsData(List<StockTransaction> stockTransactions,
                                          List<AccountTransaction> accountTransactions) {
    }

    @RequestMapping(value = "/bulk", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> execute(@RequestBody ConsolidateActionsData data) {

        List<Transaction> transactions = Transaction.convertFromAccountTransaction(data.stockTransactions);

        try {
            transactionService.bulkSave(transactions);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Received successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("Received error " + e.getMessage()));
        }
    }

    @GetMapping("/transactions/{year}")
    public List<Transaction> getTransactionsBeforeYearInclusive(
            @RequestParam String symbol,
            @PathVariable("year") int year) {
        return transactionService.getTransactionsBySymbol(symbol, year);
    }

    @GetMapping("/api/revenue/{year}")
    @ResponseBody
    public RevenueReport revenueForYear(@PathVariable("year") int year) {
        Set<String> sellingSymbolsForYear = transactionService.getSellingSymbols(year);
        Map<String, ProfitLoss> yearResult = transactionService.calculateAnnualRevenue(sellingSymbolsForYear, year);

        int yearProfit = 0;
        int yearLoss = 0;

        for (ProfitLoss profitLoss : yearResult.values()) {
            yearProfit += profitLoss.profit;
            yearLoss += profitLoss.loss;
        }

        return new RevenueReport(yearResult, yearProfit / 100.0, yearLoss / 100.0, (yearProfit - yearLoss)/100.0);
    }
}
