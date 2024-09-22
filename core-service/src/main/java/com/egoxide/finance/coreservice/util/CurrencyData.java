package com.egoxide.finance.coreservice.util;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class CurrencyData {

    @JsonProperty("year")
    private Map<String, List<Double>>  rateByYear;

    public Map<String, List<Double>> getRateByYear() {
        return rateByYear;
    }

    public void setRateByYear(Map<String, List<Double>> rateByYear) {
        this.rateByYear = rateByYear;
    }

}