package dev.delous.finance;

import dev.delous.finance.console.Cli;
import dev.delous.finance.core.AuthManager;
import dev.delous.finance.core.MoneyManager;
import dev.delous.finance.core.ReportBuilder;
import dev.delous.finance.store.MemoryAccountsStore;

public class App {
    public static void main(String[] args) {
        var store = new MemoryAccountsStore();

        var auth = new AuthManager(store);
        var money = new MoneyManager(store);
        var reports = new ReportBuilder();

        var cli = new Cli(auth, money, reports);
        cli.run();
    }
}
