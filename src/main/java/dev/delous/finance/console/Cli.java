package dev.delous.finance.console;

import dev.delous.finance.core.AuthManager;
import dev.delous.finance.core.MoneyManager;
import dev.delous.finance.core.ReportBuilder;
import dev.delous.finance.core.sink.FileTarget;
import dev.delous.finance.core.sink.OutputTarget;
import dev.delous.finance.core.sink.StdoutTarget;
import dev.delous.finance.model.Entry;
import dev.delous.finance.model.EntryType;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Cli {
    private final AuthManager auth;
    private final MoneyManager money;
    private final ReportBuilder reports;

    private final Session session = new Session();
    private final Scanner in = new Scanner(System.in);

    private OutputTarget out = new StdoutTarget();

    public Cli(AuthManager auth, MoneyManager money, ReportBuilder reports) {
        this.auth = auth;
        this.money = money;
        this.reports = reports;
    }

    public void run() {
        print("""
                === Finance CLI (in-memory) ===
                help  - список команд
                """);

        while (true) {
            print(session.isLoggedIn() ? "finance@" + session.user().get().login() + "> " : "finance> ");
            String line = readLineTrim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "exit", "quit" -> { print("Пока!\n"); return; }
                    case "help" -> help();
                    case "register" -> register();
                    case "login" -> login();
                    case "logout" -> { session.logout(); print("Вы вышли.\n"); }

                    case "out" -> setOutput();

                    case "income" -> addEntry(EntryType.INCOME);
                    case "expense" -> addEntry(EntryType.EXPENSE);

                    case "cat-add" -> addCategory();
                    case "cat-list" -> listCategories();
                    case "budget" -> setBudget();

                    case "summary" -> summary();
                    case "by-cat" -> byCategory();

                    default -> print("Неизвестная команда. Напишите help.\n");
                }
            } catch (IllegalStateException ex) {
                print("Ошибка: " + ex.getMessage() + "\n");
            } catch (Exception ex) {
                print("Что-то пошло не так: " + ex.getClass().getSimpleName() + " - " + ex.getMessage() + "\n");
            }
        }
    }

    private void help() {
        print("""
                Команды:
                  register           регистрация
                  login              вход
                  logout             выход
                  out                вывод: console или file

                Операции:
                  income             добавить доход
                  expense            добавить расход

                Категории/бюджет:
                  cat-add            создать категорию расходов
                  cat-list           список категорий
                  budget             установить бюджет (лимит) на категорию

                Отчёты:
                  summary            общие суммы доходов/расходов
                  by-cat             отчёт по категориям

                  exit | quit        выход
                """);
    }

    private void requireLogin() {
        if (!session.isLoggedIn()) {
            throw new IllegalStateException("Сначала нужно войти (login).");
        }
    }

    private void register() {
        print("Логин: ");
        String login = readLineTrim();

        print("Пароль: ");
        String password = readLineTrim();

        var created = auth.register(login, password);
        session.login(created);
        print("Готово. Вы вошли как " + created.login() + ".\n");
    }

    private void login() {
        print("Логин: ");
        String login = readLineTrim();

        print("Пароль: ");
        String password = readLineTrim();

        var user = auth.login(login, password);
        session.login(user);
        print("Успешный вход.\n");
    }

    private void setOutput() {
        print("Куда выводить? (console/file): ");
        String t = readLineTrim().toLowerCase();
        if (t.equals("console")) {
            out = new StdoutTarget();
            print("Теперь вывод в консоль.\n");
            return;
        }
        if (t.equals("file")) {
            print("Путь к файлу (например report.txt): ");
            String p = readLineTrim();
            out = new FileTarget(Path.of(p));
            print("Теперь вывод в файл: " + p + "\n");
            return;
        }
        print("Не понял. Введите console или file.\n");
    }

    private void addEntry(EntryType type) {
        requireLogin();

        print("Сумма: ");
        BigDecimal amount = readMoney();

        String category = null;
        if (type == EntryType.EXPENSE) {
            print("Категория: ");
            category = readLineTrim();
        }

        print("Комментарий (можно пусто): ");
        String note = in.nextLine().trim();

        var e = new Entry(type, amount, category, note, LocalDateTime.now());
        money.addEntry(session.user().get().id(), e);

        print("Записано.\n");
    }

    private void addCategory() {
        requireLogin();
        print("Название категории: ");
        String name = readLineTrim();
        money.addCategory(session.user().get().id(), name);
        print("Категория добавлена.\n");
    }

    private void listCategories() {
        requireLogin();
        var cats = money.categories(session.user().get().id());

        if (cats.isEmpty()) {
            print("Категорий нет.\n");
            return;
        }

        TextTable tt = new TextTable();
        tt.addRow("Категория", "Лимит", "Потрачено", "Остаток");
        for (var c : cats) {
            var spent = money.spentByCategory(session.user().get().id(), c.name());
            var limit = c.limit() == null ? "—" : c.limit().toPlainString();
            var spentS = spent.toPlainString();
            var left = c.limit() == null ? "—" : c.limit().subtract(spent).toPlainString();
            tt.addRow(c.name(), limit, spentS, left);
        }

        out.write(tt.render());
    }

    private void setBudget() {
        requireLogin();
        print("Категория: ");
        String name = readLineTrim();

        print("Лимит: ");
        BigDecimal limit = readMoney();

        money.setBudget(session.user().get().id(), name, limit);
        print("Лимит установлен.\n");
    }

    private void summary() {
        requireLogin();
        var wallet = money.wallet(session.user().get().id());

        var report = reports.summary(wallet);
        out.write(report + "\n");
    }

    private void byCategory() {
        requireLogin();
        var wallet = money.wallet(session.user().get().id());
        var report = reports.byCategory(wallet);
        out.write(report + "\n");
    }

    private BigDecimal readMoney() {
        while (true) {
            String raw = readLineTrim().replace(',', '.');
            try {
                BigDecimal v = new BigDecimal(raw);
                if (v.signum() <= 0) {
                    print("Сумма должна быть > 0. Повторите: ");
                    continue;
                }
                return v;
            } catch (NumberFormatException ex) {
                print("Неверное число. Повторите: ");
            }
        }
    }

    private String readLineTrim() {
        return in.nextLine().trim();
    }

    private void print(String s) {
        System.out.print(s);
    }
}
