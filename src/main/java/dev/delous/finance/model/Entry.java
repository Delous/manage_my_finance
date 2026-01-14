package dev.delous.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Entry(
        EntryType type,
        BigDecimal amount,
        String category,     // только для EXPENSE (для INCOME может быть null)
        String note,
        LocalDateTime at
) { }
