package dev.delous.finance.core;

import dev.delous.finance.console.TextTable;
import dev.delous.finance.model.EntryType;
import dev.delous.finance.model.WalletBook;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportBuilder {

    public String summary(WalletBook wallet) {
        BigDecimal income = wallet.entries().stream()
                .filter(e -> e.type() == EntryType.INCOME)
                .map(e -> e.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = wallet.entries().stream()
                .filter(e -> e.type() == EntryType.EXPENSE)
                .map(e -> e.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = income.subtract(expense);

        return """
                === Сводка ===
                Доходы : %s
                Расходы: %s
                Баланс : %s
                """.formatted(income.toPlainString(), expense.toPlainString(), balance.toPlainString());
    }

    public String byCategory(WalletBook wallet) {
        Map<String, BigDecimal> spent = new LinkedHashMap<>();
        wallet.categories().forEach(c -> spent.put(c.name(), BigDecimal.ZERO));

        wallet.entries().stream()
                .filter(e -> e.type() == EntryType.EXPENSE)
                .forEach(e -> spent.merge(e.category(), e.amount(), BigDecimal::add));

        TextTable t = new TextTable();
        t.addRow("Категория", "Потрачено", "Лимит", "Остаток");

        wallet.categories().forEach(c -> {
            BigDecimal s = spent.getOrDefault(c.name(), BigDecimal.ZERO);
            String limit = c.limit() == null ? "—" : c.limit().toPlainString();
            String left = c.limit() == null ? "—" : c.limit().subtract(s).toPlainString();
            t.addRow(c.name(), s.toPlainString(), limit, left);
        });

        return "=== По категориям ===\n" + t.render();
    }
}
