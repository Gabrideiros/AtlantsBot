package me.gabrideiros.bot.util.reaction;

import net.dv8tion.jda.api.entities.Message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ReactionListener<T> {

    private final Map<String, Consumer<Message>> reactions;
    private final long userId;
    private volatile T data;
    private Long expiresIn, lastAction;
    private boolean active;

    public ReactionListener(long userId, T data) {
        this.data = data;
        this.userId = userId;
        reactions = new LinkedHashMap<>();
        active = true;
        lastAction = System.currentTimeMillis();
        expiresIn = TimeUnit.MINUTES.toMillis(5);
    }

    public boolean isActive() {
        return active;
    }

    public void disable() {
        this.active = false;
    }

    public void setExpiresIn(TimeUnit timeUnit, long time) {
        expiresIn = timeUnit.toMillis(time);
    }

    public boolean hasReaction(String emote) {
        return reactions.containsKey(emote);
    }

    public void react(String emote, Message message) {
        if (hasReaction(emote)) reactions.get(emote).accept(message);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void registerReaction(String emote, Consumer<Message> consumer) {
        reactions.put(emote, consumer);
    }

    public Set<String> getEmotes() {
        return reactions.keySet();
    }

    public void updateLastAction() {
        lastAction = System.currentTimeMillis();
    }

    public Long getExpiresInTimestamp() {
        return lastAction + expiresIn;
    }

    public long getUserId() {
        return userId;
    }
}
