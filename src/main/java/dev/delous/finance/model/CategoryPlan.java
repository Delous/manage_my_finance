package dev.delous.finance.model;

import java.math.BigDecimal;

public record CategoryPlan(
        String name,
        BigDecimal limit // может быть null, если лимит не задан
) { }
