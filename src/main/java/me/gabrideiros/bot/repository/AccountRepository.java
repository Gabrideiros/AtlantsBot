package me.gabrideiros.bot.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import me.gabrideiros.bot.Main;
import me.gabrideiros.bot.model.Account;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@AllArgsConstructor
public class AccountRepository implements Repository<Account> {

    private Main plugin;
    private final Gson gson;

    @Override
    public Optional<Account> find(Long name, Account account) {
        return Optional.empty();
    }

    @Override
    public void insert(Account account) {

        CompletableFuture.runAsync(() ->
                plugin.getStorage().sendCommand("INSERT INTO bot (user, nickname, invites) VALUES ('" + account.getUser() + "', '" + account.getNickname() + "', '" + gson.toJson(account.getInvites()) + "');"));

    }

    @Override
    public void update(Account account) {

        CompletableFuture.runAsync(() ->
                plugin.getStorage().sendCommand("UPDATE bot SET invites = '" + account.getInvites() + "' WHERE user = '" + account.getUser() + "'"));

    }

    @Override
    public void delete(Account account) {

        CompletableFuture.runAsync(() ->
                plugin.getStorage().sendCommand("DELETE FROM bot WHERE uuid = '" + account.getUser() + "'"));

    }

    @Override
    public Map<Long, Account> load() {

        Map<Long, Account> accounts = new HashMap<>();

        System.out.println("[Bot] Carregando todas as contas...");

        ResultSet rs = plugin.getStorage().sendQuery("SELECT * FROM bot");

        try {
            while (rs.next()) {

                long user = rs.getLong("user");
                String nickname = rs.getString("nickname");

                String invites = rs.getString("invites");

                Type type = new TypeToken<List<String>>() {
                }.getType();

                List<String> lit = this.gson.fromJson(invites, type);

                Account account = Account.builder()
                        .user(user)
                        .nickname(nickname)
                        .invites(lit)
                        .build();

                accounts.put(user, account);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[Bot] Nao foi possivel carregar todas as contas!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        } finally {
            plugin.getLogger().log(Level.INFO, "[PrismCash] Foram carregadas {0} conta(s)!", accounts.size());

        }
        return accounts;
    }
}
