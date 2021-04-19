package me.gabrideiros.bot.repository;

import java.util.Map;
import java.util.Optional;

public interface Repository<T> {

    Optional<T> find(Long name, T t);

    void insert(T t);

    void update(T t);

    void delete(T t);

    Map<Long, T> load();

}
