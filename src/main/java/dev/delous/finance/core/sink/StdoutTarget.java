package dev.delous.finance.core.sink;

public class StdoutTarget implements OutputTarget {
    @Override
    public void write(String text) {
        System.out.print(text);
    }
}
