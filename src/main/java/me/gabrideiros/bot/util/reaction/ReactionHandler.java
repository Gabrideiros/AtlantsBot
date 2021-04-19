package me.gabrideiros.bot.util.reaction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReactionHandler {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, ReactionListener<?>>> reactions;

    public ReactionHandler() {
        reactions = new ConcurrentHashMap<>();
    }

    public synchronized void addReactionListener(long userID, Message message, ReactionListener<?> handler) {
        if (handler == null) {
            return;
        }
        if (message.getChannelType().equals(ChannelType.TEXT)) {
            if (!PermissionUtil.checkPermission(message.getTextChannel(), message.getGuild().getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        if (!reactions.containsKey(userID)) {
            reactions.put(userID, new ConcurrentHashMap<>());
        }
        if (!reactions.get(userID).containsKey(message.getIdLong())) {
            for (String emote : handler.getEmotes()) {
                message.addReaction(emote).queue();
            }
        }
        reactions.get(userID).put(message.getIdLong(), handler);
    }

    public synchronized void removeReactionListener(long userID, long messageId) {
        if (!reactions.containsKey(userID)) return;
        reactions.remove(messageId);
    }

    public void handle(MessageChannel channel, long messageId, long userId, MessageReaction reaction) {

        ReactionListener<?> listener = reactions.get(userId).get(messageId);

        if (!listener.isActive() || listener.getExpiresInTimestamp() < System.currentTimeMillis()) {
            reactions.remove(messageId);
        } else if ((listener.hasReaction(reaction.getReactionEmote().getName())) && listener.getUserId() == userId) {
            reactions.get(userId).get(messageId).updateLastAction();
            Message message = channel.retrieveMessageById(messageId).complete();
            listener.react(reaction.getReactionEmote().getName(), message);
        }
    }

    public boolean canHandle(long userID, long messageId) {
        return reactions.containsKey(userID) && reactions.get(userID).containsKey(messageId);
    }

    public synchronized void removeUser(long userID) {
        reactions.remove(userID);
    }

    public synchronized void cleanCache() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<Long, ConcurrentHashMap<Long, ReactionListener<?>>>> iterator = reactions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Long, ConcurrentHashMap<Long, ReactionListener<?>>> mapEntry = iterator.next();
            mapEntry.getValue().values().removeIf(listener -> !listener.isActive() || listener.getExpiresInTimestamp() < now);
            if (mapEntry.getValue().values().isEmpty()) {
                reactions.remove(mapEntry.getKey());
            }
        }
    }
}