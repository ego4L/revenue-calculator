package com.egoxide.finance.fileuploadservice.domain;

public record PositiveDouble(double value) {
    public PositiveDouble {
        if (value < 0) {
            throw new IllegalArgumentException("Negative double value: " + value);
        }
    }
}
