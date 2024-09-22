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
import java.util.Set;


@RestController
public class DataController {

    private final TransactionService transactionService;

    private record RevenueValue(double revenue) { }

    public DataController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private record AccountTransaction(LocalDateTime dateTime, int amount, Action action) { }

    private record ConsolidateActionsData(List<StockTransaction> stockTransactions,
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
        return transactionService.getTransactionsBySymbolBeforeYearInclusive(symbol, year);
    }

    @GetMapping("/api/revenue/{year}")
    @ResponseBody
    public RevenueValue revenueForYear(@PathVariable("year") int year) {
        Set<String> sellingSymbolsForYear = transactionService.getSellingSymbolsForYear(year);
        return new RevenueValue(transactionService.calculateAnnualRevenue(sellingSymbolsForYear, year));
    }
}
