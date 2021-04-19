package me.gabrideiros.bot.command;

import lombok.AllArgsConstructor;
import me.gabrideiros.bot.Main;
import me.gabrideiros.bot.model.Account;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class Integrate implements CommandExecutor {

    private Main plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        Optional<Account> account = plugin.getController().getByNickname(player.getName());

        if (account.isPresent()) {
            player.sendMessage("§cVocê já possui uma conta do discord vinculada!");
            return true;
        }

        Optional<String> hasCode = plugin.getCodes().values().stream().filter(value -> value.equalsIgnoreCase(player.getName())).findAny();

        if (hasCode.isPresent()) {
            player.sendMessage("§cVocê já possuí uma vinculção pendente!");
            return true;
        }

        String code = UUID.randomUUID().toString().substring(30).replace("-", "").toUpperCase();
        plugin.getCodes().put(code, player.getName());

        player.sendMessage(new String[]{
                "§aMuito bem! Acesse o nosso discord e execute o comando '.vincular " + code + "'",
                "§ano canal 'comandos' para vincular a sua conta do discord."
        });

        return true;
    }
}
