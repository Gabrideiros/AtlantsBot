package me.gabrideiros.bot.controller;

import java.util.Map;
import java.util.Optional;

public interface Controller<T> {

    Optional<T> getByUser(Long name);

    Optional<T> getByNickname(String nickname);

    Map<Long, T> getAccounts();

}

