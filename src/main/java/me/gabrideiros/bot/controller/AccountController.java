package me.gabrideiros.bot.controller;

import lombok.AllArgsConstructor;
import me.gabrideiros.bot.model.Account;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class AccountController implements Controller<Account> {

    private Map<Long, Account> accounts;

    @Override
    public Optional<Account> getByUser(Long name) {
        return Optional.ofNullable(accounts.get(name));
    }

    @Override
    public Optional<Account> getByNickname(String nickname) {
        return accounts.values().stream().filter(account -> account.getNickname().equals(nickname)).findAny();
    }

    @Override
    public Map<Long, Account> getAccounts() {
        return accounts;
    }

}
