package dev.delous.finance.console;

import java.util.ArrayList;
import java.util.List;

public class TextTable {
    private final List<String[]> rows = new ArrayList<>();

    public void addRow(String... cols) {
        rows.add(cols);
    }

    public String render() {
        if (rows.isEmpty()) return "";

        int colsCount = rows.stream().mapToInt(r -> r.length).max().orElse(0);
        int[] widths = new int[colsCount];

        for (String[] row : rows) {
            for (int c = 0; c < row.length; c++) {
                String v = row[c] == null ? "" : row[c];
                widths[c] = Math.max(widths[c], v.length());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String[] row : rows) {
            for (int c = 0; c < colsCount; c++) {
                String v = c < row.length ? (row[c] == null ? "" : row[c]) : "";
                sb.append(padRight(v, widths[c]));
                if (c != colsCount - 1) sb.append("  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String padRight(String s, int w) {
        if (s.length() >= w) return s;
        return s + " ".repeat(w - s.length());
    }
}
