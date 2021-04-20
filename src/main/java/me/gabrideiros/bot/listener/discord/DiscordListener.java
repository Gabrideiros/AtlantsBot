package me.gabrideiros.bot.listener.discord;

import lombok.AllArgsConstructor;
import me.gabrideiros.bot.Main;
import me.gabrideiros.bot.model.Account;
import me.gabrideiros.bot.model.Reward;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class DiscordListener extends ListenerAdapter {

    private Main plugin;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        plugin.getGuildInvites().put(event.getGuild().getId(), event.getGuild().retrieveInvites().complete());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        User author = event.getAuthor();

        if (author.isBot()) return;

        Message message = event.getMessage();

        String[] args = message.getContentRaw().split(" ");

        if (args[0].equalsIgnoreCase(".help")) {

            Optional<Account> optional = plugin.getController().getByUser(author.getIdLong());

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("**Comandos**")
                    .setDescription("\n`.vincular <código>` - Para vincular sua conta do jogo.\n`.convites` - Para ver a quantidade de convites.\n")
                    .build();

            message.getChannel().sendMessage(embed);
            return;
        }

        else if (args[0].equalsIgnoreCase(".convites")) {

            Optional<Account> optional = plugin.getController().getByUser(author.getIdLong());

            if (!optional.isPresent()) {
                message.reply("Você não possui uma conta vinculada!");
                return;
            }

            Account account = optional.get();

            message.reply("Você possui ´" + account.getInvites().size() + "´ convites.");
            return;
        }

        else if (args[0].equalsIgnoreCase(".vincular")) {

            if (args.length > 2) {
                message.reply("Utilize .vincular `<código>`.");
                return;
            }

            Optional<String> code = plugin.getCodes().keySet().stream().filter(value -> value.equalsIgnoreCase(args[1])).findAny();

            if (!code.isPresent()) {
                message.reply("Não existe nenhuma integração pendente com este código!").queue();
                return;
            }

            String nickname = plugin.getCodes().get(code.get());

            Account account = Account.builder()
                    .user(author.getIdLong())
                    .nickname(nickname)
                    .invites(new ArrayList<>())
                    .build();

            plugin.getController().getAccounts().put(author.getIdLong(), account);
            plugin.getRepository().insert(account);

            plugin.getCodes().remove(code.get());

            message.reply("Sua conta foi vinculada com o nickname: " + nickname + ".").queue();
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

        if (plugin.getReactionHandler().canHandle(event.getUserIdLong(), event.getMessageIdLong()))
            plugin.getReactionHandler().handle(event.getChannel(), event.getMessageIdLong(), event.getUserIdLong(), event.getReaction());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        TextChannel channel = event.getGuild().getTextChannelById(plugin.getConfig().getString("Channel"));

        List<Invite> lastInvites = plugin.getGuildInvites().get(event.getGuild().getId());
        List<Invite> newInvites = event.getGuild().retrieveInvites().complete();

        Invite current = getInvites(lastInvites, newInvites);

        if (current == null) return;

        Optional<Account> optional = plugin.getController().getByUser(current.getInviter().getIdLong());

        if (!optional.isPresent()) return;

        Account account = optional.get();

        if (!account.getInvites().contains(event.getUser().getId())) {
            account.getInvites().add(event.getUser().getId());

            plugin.getRepository().update(account);

            for (Reward value : plugin.getRewards().values()) {

                if (account.getInvites().size() != value.getAmount()) continue;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    value.getCommands().forEach(command -> Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("<player>", account.getNickname())));
                }, 20L);

                if (channel == null) return;

                channel.sendMessage("O usuário `" + current.getInviter().getAsTag() + "` ganhou um prêmio por convidar `" + value.getAmount() + "` pessoas.").queue();

            }
        }

        if (channel == null) return;

        channel.sendMessage("O usuário `" + event.getUser().getAsTag() + "` foi convidado por `" + current.getInviter().getAsTag() + "`.").queue();

    }

    @Override
    public void onGuildMemberLeave(@NotNull GuildMemberLeaveEvent event) {

        Optional<Account> optional = plugin.getController().getByUser(event.getUser().getIdLong());

        if (!optional.isPresent()) return;

        plugin.getRepository().delete(optional.get());
        plugin.getController().getAccounts().remove(event.getUser().getIdLong());

    }

    public Invite getInvites(List<Invite> lastInvites, List<Invite> newInvite) {

        for (Invite invite : newInvite) {
            for (Invite lastInvite : lastInvites) {
                if (lastInvite.getCode().equals(invite.getCode())) {
                    if (invite.getUses() > lastInvite.getUses()) return invite;
                }
            }
        }
        return null;
    }
}
