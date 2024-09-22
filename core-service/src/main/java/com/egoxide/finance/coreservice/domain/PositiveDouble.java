package com.egoxide.finance.coreservice.domain;

public record PositiveDouble(double value) {
    public PositiveDouble {
        if (value < 0) {
            throw new IllegalArgumentException("Negative double value: " + value);
        }
    }
}
