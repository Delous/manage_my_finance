package dev.delous.finance.store;

import dev.delous.finance.model.UserAccount;

import java.util.*;

public class MemoryAccountsStore implements AccountsStore {
    private final Map<UUID, UserAccount> byId = new HashMap<>();
    private final Map<String, UUID> idByLogin = new HashMap<>();

    @Override
    public Optional<UserAccount> findByLogin(String login) {
        UUID id = idByLogin.get(login);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void save(UserAccount user) {
        byId.put(user.id(), user);
        idByLogin.put(user.login(), user.id());
    }
}
