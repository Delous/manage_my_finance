package dev.delous.finance.console;

import dev.delous.finance.model.UserAccount;

import java.util.Optional;

public class Session {
    private UserAccount current;

    public boolean isLoggedIn() {
        return current != null;
    }

    public Optional<UserAccount> user() {
        return Optional.ofNullable(current);
    }

    public void login(UserAccount user) {
        this.current = user;
    }

    public void logout() {
        this.current = null;
    }
}
