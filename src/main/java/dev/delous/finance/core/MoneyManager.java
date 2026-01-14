package dev.delous.finance.core;

import dev.delous.finance.model.CategoryPlan;
import dev.delous.finance.model.Entry;
import dev.delous.finance.model.EntryType;
import dev.delous.finance.model.WalletBook;
import dev.delous.finance.store.AccountsStore;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MoneyManager {
    private final AccountsStore store;

    public MoneyManager(AccountsStore store) {
        this.store = store;
    }

    public WalletBook wallet(UUID userId) {
        return store.findById(userId).orElseThrow(() -> new IllegalStateException("Сессия битая: userId не найден.")).wallet();
    }

    public void addEntry(UUID userId, Entry entry) {
        if (entry.amount() == null || entry.amount().signum() <= 0) {
            throw new IllegalStateException("Сумма должна быть > 0.");
        }
        if (entry.type() == EntryType.EXPENSE && (entry.category() == null || entry.category().isBlank())) {
            throw new IllegalStateException("Для расхода нужна категория.");
        }
        wallet(userId).addEntry(entry);
    }

    public void addCategory(UUID userId, String name) {
        if (name == null || name.isBlank()) throw new IllegalStateException("Категория пустая.");
        wallet(userId).addCategory(name.trim());
    }

    public List<CategoryPlan> categories(UUID userId) {
        return wallet(userId).categories().stream().collect(Collectors.toList());
    }

    public void setBudget(UUID userId, String category, BigDecimal limit) {
        if (category == null || category.isBlank()) throw new IllegalStateException("Категория пустая.");
        if (limit == null || limit.signum() <= 0) throw new IllegalStateException("Лимит должен быть > 0.");
        wallet(userId).setLimit(category.trim(), limit);
    }

    public BigDecimal spentByCategory(UUID userId, String category) {
        var w = wallet(userId);
        return w.entries().stream()
                .filter(e -> e.type() == EntryType.EXPENSE)
                .filter(e -> category != null && category.equals(e.category()))
                .map(Entry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
