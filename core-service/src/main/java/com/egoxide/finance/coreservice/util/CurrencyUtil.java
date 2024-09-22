package com.egoxide.finance.coreservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.*;

import java.util.List;
import java.util.Map;

public class CurrencyUtil {

    public static Map<String, List<Double>> getCurrencyRateForYear(String toCurrency, String fromCurrency) {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, List<Double>> result;

        File file;
        try {
            file = ResourceUtils.getFile("classpath:currencies/" + toCurrency + "-" + fromCurrency + ".json");
            try (InputStream in = new FileInputStream(file)) {
                CurrencyData currencyData = mapper.readValue(in, CurrencyData.class);
                result = currencyData.getRateByYear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
