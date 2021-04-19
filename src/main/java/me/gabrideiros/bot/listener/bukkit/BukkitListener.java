package me.gabrideiros.bot.listener.bukkit;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsEnteredInAttack;
import lombok.AllArgsConstructor;
import me.gabrideiros.bot.Main;
import me.gabrideiros.bot.model.Account;
import me.gabrideiros.bot.util.reaction.ReactionListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class BukkitListener implements Listener {

    private Main plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        Optional<Account> account = plugin.getController().getByNickname(player.getName());

        if (!account.isPresent()) return;

        User user = plugin.getJda().retrieveUserById(account.get().getUser()).complete();

        MessageEmbed embed = new EmbedBuilder()
                .setImage(plugin.getConfig().getString("Authenticator"))
                .build();

        user.openPrivateChannel().complete().sendMessage(embed).queue(message -> {

            ReactionListener<String> handler = new ReactionListener<String>(user.getIdLong(), message.getId());
            handler.setExpiresIn(TimeUnit.SECONDS, 15);
            handler.registerReaction("✅", (ret) -> {
                message.getChannel().sendMessage("Entrada autorizada com sucesso!").queue();
                plugin.getReactionHandler().removeUser(user.getIdLong());
            });
            handler.registerReaction("❌",  (ret) -> {


                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if(player !=null || player.isOnline()) player.kickPlayer("§cParece que você não é o dono desta conta!");
                }, 20L);

                message.getChannel().sendMessage("Entrada negada com sucesso!").queue();
                plugin.getReactionHandler().removeUser(user.getIdLong());
            });

            plugin.getReactionHandler().addReactionListener(user.getIdLong(), message, handler);
        });

        return;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        Optional<String> code = plugin.getCodes().values().stream().filter(name -> name.equals(player.getName())).findAny();

        if (code.isPresent()) plugin.getCodes().remove(code.get());

    }
    @EventHandler
    public void onAttack(EventFactionsEnteredInAttack event) {

        Faction faction = event.getFaction();

        for (MPlayer mPlayer : faction.getMPlayers()) {

            Optional<Account> account = plugin.getController().getByNickname(mPlayer.getName());

            if (!account.isPresent()) continue;

            User user = plugin.getJda().retrieveUserById(account.get().getUser()).complete();

            user.openPrivateChannel().complete().sendMessage(
                    new EmbedBuilder()
                    .setImage(plugin.getConfig().getString("Attack"))
                    .build()
            ).queue();
        }
    }
}
