package me.gabrideiros.bot;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.gabrideiros.bot.command.Integrate;
import me.gabrideiros.bot.controller.AccountController;
import me.gabrideiros.bot.controller.Controller;
import me.gabrideiros.bot.database.Storage;
import me.gabrideiros.bot.database.types.MySQL;
import me.gabrideiros.bot.database.types.SQLite;
import me.gabrideiros.bot.listener.bukkit.BukkitListener;
import me.gabrideiros.bot.listener.discord.DiscordListener;
import me.gabrideiros.bot.model.Reward;
import me.gabrideiros.bot.repository.AccountRepository;
import me.gabrideiros.bot.repository.Repository;
import me.gabrideiros.bot.util.SectionBuilder;
import me.gabrideiros.bot.util.reaction.ReactionHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Getter
public class Main extends JavaPlugin {

    private JDA jda;

    private Storage storage;

    private Repository repository;
    private Controller controller;


    private Map<String, Reward> rewards;

    private Map<String, String> codes;

    private Map<String, List<Invite>> guildInvites;

    private ReactionHandler reactionHandler;

    public Main() {

        rewards = new HashMap<>();

        codes = new HashMap<>();

        guildInvites = new HashMap<>();

    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        loadJDA();

        openStorage();

        repository = new AccountRepository(this, new GsonBuilder().create());
        controller = new AccountController(repository.load());

        reactionHandler = new ReactionHandler();

        loadRewards();

        Bukkit.getPluginManager().registerEvents(new BukkitListener(this), this);

        getCommand("vincular").setExecutor(new Integrate(this));

    }

    public void loadJDA() {
        try {
            jda = JDABuilder.createDefault(
                    getConfig().getString("Key")
            ).enableIntents(GatewayIntent.GUILD_MEMBERS
            ).addEventListeners(
                    new DiscordListener(this)
            ).build();

        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void loadRewards() {

        List<Reward> list = SectionBuilder.of(Reward.class, getConfig().getConfigurationSection("Rewards"))
                .parameter("Amount", int.class)
                .parameter("Commands", SectionBuilder.StringList.class)
                .build();

                list.forEach(reward -> rewards.put(reward.getKey(), reward));
                getLogger().log(Level.INFO, "Foram carregadas {0} recompensas.", list.size());
    }

    private void openStorage() {

        ConfigurationSection cs = getConfig().getConfigurationSection("Connection");

        if (cs.getString("Type").equalsIgnoreCase("MySQL"))
            storage = new MySQL(this, cs.getString("Host"), cs.getInt("Port"), cs.getString("Database"), cs.getString("Username"), cs.getString("Password"));
        else
            storage = new SQLite(this, "database.db");

        storage.openConnection();

        storage.sendCommand("CREATE TABLE IF NOT EXISTS bot (user long, nickname varchar(32), invites longtext);");

    }
}
