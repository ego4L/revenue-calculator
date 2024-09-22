package com.egoxide.finance.coreservice.domain;

import java.time.LocalDateTime;

public record StockTransaction(LocalDateTime dateTime, String symbol, PositiveInt price, PositiveDouble quantity,
                               Action action) { }

