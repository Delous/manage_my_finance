package dev.delous.finance.core;

import dev.delous.finance.model.UserAccount;
import dev.delous.finance.model.WalletBook;
import dev.delous.finance.store.AccountsStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class AuthManager {
    private final AccountsStore store;

    public AuthManager(AccountsStore store) {
        this.store = store;
    }

    public UserAccount register(String login, String password) {
        if (login == null || login.isBlank()) throw new IllegalStateException("Логин пустой.");
        if (password == null || password.isBlank()) throw new IllegalStateException("Пароль пустой.");

        store.findByLogin(login).ifPresent(u -> {
            throw new IllegalStateException("Такой логин уже занят.");
        });

        var user = new UserAccount(
                UUID.randomUUID(),
                login.trim(),
                hash(password),
                new WalletBook()
        );
        store.save(user);
        return user;
    }

    public UserAccount login(String login, String password) {
        var user = store.findByLogin(login).orElseThrow(() -> new IllegalStateException("Пользователь не найден."));
        if (!user.passwordHash().equals(hash(password))) {
            throw new IllegalStateException("Неверный пароль.");
        }
        return user;
    }

    private static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось захэшировать пароль.");
        }
    }
}
