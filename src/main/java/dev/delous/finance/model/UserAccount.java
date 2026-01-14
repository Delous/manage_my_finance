package dev.delous.finance.model;

import java.util.UUID;

public record UserAccount(
        UUID id,
        String login,
        String passwordHash,
        WalletBook wallet
) { }
