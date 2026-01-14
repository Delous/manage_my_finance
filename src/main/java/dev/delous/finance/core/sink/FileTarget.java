package dev.delous.finance.core.sink;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileTarget implements OutputTarget {
    private final Path path;

    public FileTarget(Path path) {
        this.path = path;
    }

    @Override
    public void write(String text) {
        try {
            Files.writeString(path, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать в файл: " + path);
        }
    }
}
