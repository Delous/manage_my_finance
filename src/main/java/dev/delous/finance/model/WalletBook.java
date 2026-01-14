package dev.delous.finance.model;

import java.math.BigDecimal;
import java.util.*;

public class WalletBook {
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, CategoryPlan> expenseCategories = new LinkedHashMap<>();

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public Collection<CategoryPlan> categories() {
        return Collections.unmodifiableCollection(expenseCategories.values());
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        if (entry.type() == EntryType.EXPENSE && entry.category() != null) {
            expenseCategories.putIfAbsent(entry.category(), new CategoryPlan(entry.category(), null));
        }
    }

    public void addCategory(String name) {
        expenseCategories.putIfAbsent(name, new CategoryPlan(name, null));
    }

    public void setLimit(String name, BigDecimal limit) {
        var existing = expenseCategories.get(name);
        if (existing == null) {
            expenseCategories.put(name, new CategoryPlan(name, limit));
        } else {
            expenseCategories.put(name, new CategoryPlan(existing.name(), limit));
        }
    }
}
