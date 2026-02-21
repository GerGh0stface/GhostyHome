package de.ghosty.ghostyhome;

import de.ghosty.ghostyhome.commands.*;
import de.ghosty.ghostyhome.listener.GUIListener;
import de.ghosty.ghostyhome.manager.HomeManager;
import de.ghosty.ghostyhome.manager.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GhostyHome extends JavaPlugin {

    private static GhostyHome instance;
    private HomeManager homeManager;
    private LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        langManager = new LangManager(this);
        homeManager = new HomeManager(this);

        // Register commands
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("adminhome").setExecutor(new AdminHomeCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("GhostyHome v" + getDescription().getVersion() + " wurde erfolgreich gestartet!");
        getLogger().info("Sprache: " + getConfig().getString("language", "de_DE"));
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveHomes();
        }
        getLogger().info("GhostyHome wurde deaktiviert.");
    }

    public static GhostyHome getInstance() {
        return instance;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }
}
