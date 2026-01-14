package dev.delous.finance.store;

import dev.delous.finance.model.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface AccountsStore {
    Optional<UserAccount> findByLogin(String login);
    Optional<UserAccount> findById(UUID id);
    void save(UserAccount user);
}
