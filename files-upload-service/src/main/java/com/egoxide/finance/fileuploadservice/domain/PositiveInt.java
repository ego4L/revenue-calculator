package com.egoxide.finance.fileuploadservice.domain;

public record PositiveInt(int value) {

    public PositiveInt {
        if (value < 0) {
            throw new IllegalArgumentException("Negative integer value: " + value);
        }
    }

}
