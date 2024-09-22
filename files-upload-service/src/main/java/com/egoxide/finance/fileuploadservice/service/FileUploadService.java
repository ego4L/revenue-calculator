package com.egoxide.finance.fileuploadservice.service;

import com.egoxide.finance.fileuploadservice.domain.Action;
import com.egoxide.finance.fileuploadservice.domain.StockTransaction;
import com.egoxide.finance.fileuploadservice.exception.ParseTransactionDataException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class FileUploadService {

    private static final Pattern yearLine = Pattern.compile("^\\d{4}$");
    private static final Pattern monthLine = Pattern.compile("^\t\\d{2}$");
    private static final Pattern dayLine = Pattern.compile("^\\d{2}$");
    private static final Pattern stockTransaction = Pattern.compile("^(\\d{4})\\s+(\\w{1,5})\\s+([+-]?[0-9]*[.]?[0-9]+)\\s+([+-]?[0-9]+)$");
    private static final Pattern accountTransaction = Pattern.compile("^(\\d{4})\\s+([+-]?[0-9]+)$");
    private static final Pattern commissionTransaction = Pattern.compile("^(\\d{4})\\s+COMMISSION\\s+([+-]?[0-9]+)$");

    private final List<StockTransaction> stockTransactions = new ArrayList<>();
    private final List<AccountTransaction> accountTransactions = new ArrayList<>();

    private record HoursAndMinutes(int hours, int minutes) { }
    private record AccountTransaction(LocalDateTime dateTime, int amount, Action action) { }
    private record ConsolidateActionsData(List<StockTransaction> stockTransactions, List<AccountTransaction> accountTransactions) { };


    public ResponseEntity<Object> uploadFile(MultipartFile file) throws ParseTransactionDataException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String fileLine;

            var lineNumber = 0;

            var year = 1970;
            var month = 1;
            var day = 1;

            while ((fileLine = br.readLine()) != null) {

                lineNumber++;

                if (fileLine.trim().isEmpty() || fileLine.trim().startsWith("//")) {
                    continue;
                }

                String line = fileLine.stripTrailing();

                switch (line) {
                    case String s when yearLine.matcher(line).matches() -> year = Integer.parseInt(s.trim());
                    case String s when monthLine.matcher(line).matches() -> month = Integer.parseInt(s.trim());
                    case String s when dayLine.matcher(line).matches() -> day = Integer.parseInt(s.trim());
                    case String s when stockTransaction.matcher(line).matches() -> updateStockTransactionList(s, year, month, day);
                    case String s when accountTransaction.matcher(line).matches() -> updateAccountTransactionList(s, year, month, day);
                    case String s when commissionTransaction.matcher(line).matches() -> updateCommissionTransactionList(s, year, month, day);

                    default -> throw new ParseTransactionDataException("Line: " + lineNumber + " contains unparsing content: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConsolidateActionsData consolidateActionsData = new ConsolidateActionsData(stockTransactions, accountTransactions);

        return sendJsonToCalcService(consolidateActionsData);
    }

    private static ResponseEntity<Object> sendJsonToCalcService(ConsolidateActionsData consolidateActionsData) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

        String json;

        try {
            json = ow.writeValueAsString(consolidateActionsData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<Object>(json, headers);
        return restTemplate.exchange("http://localhost:8090/bulk", HttpMethod.POST, entity, Object.class);
    }

    private void updateCommissionTransactionList(String s, int year, int month, int day) {

        Matcher matcher = commissionTransaction.matcher(s);

        if (!matcher.find()) {
            return;
        }

        var hoursAndMinutes = matcher.group(1);
        var amount = matcher.group(2);

        LocalDateTime dateTime = buildLocalDateTime(year, month, day, hoursAndMinutes);

        accountTransactions.add(new AccountTransaction(dateTime, Integer.parseInt(amount), Action.COMMISSION));
    }

    private void updateAccountTransactionList(String s, int year, int month, int day) {

        Matcher matcher = accountTransaction.matcher(s);

        if (!matcher.find()) {
            return;
        }

        var hoursAndMinutes = matcher.group(1);
        var amount = Integer.parseInt(matcher.group(2));
        var dateTime = buildLocalDateTime(year, month, day, hoursAndMinutes);

        accountTransactions.add(new AccountTransaction(dateTime, amount, amount > 0 ? Action.TOPUP : Action.WITHDRAW));
    }

    private void updateStockTransactionList(String s, int year, int month, int day) {

        Matcher matcher = stockTransaction.matcher(s);

        if (!matcher.find()) {
            return;
        }

        var hoursAndMinutes = matcher.group(1);
        var symbol = matcher.group(2);
        var quantity = matcher.group(3);
        var price = matcher.group(4);

        var dateAndTime = buildLocalDateTime(year, month, day, hoursAndMinutes);

        stockTransactions.add(StockTransaction.of(dateAndTime, symbol, quantity, price));
    }

    private static LocalDateTime buildLocalDateTime(int year, int month, int day, String hoursAndMinutes) {
        HoursAndMinutes time = parseTime(hoursAndMinutes);
        return LocalDateTime.of(year, month, day, time.hours(), time.minutes());
    }

    private static HoursAndMinutes parseTime(String hoursAndMinutes) {

        int hours = Integer.parseInt(hoursAndMinutes.substring(0, 2));;
        int minutes = Integer.parseInt(hoursAndMinutes.substring(2));

        return new HoursAndMinutes(hours, minutes);
    }
}
