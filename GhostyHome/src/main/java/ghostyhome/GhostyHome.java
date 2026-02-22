package ghostyhome;

import ghostyhome.commands.AdminHomeCommand;
import ghostyhome.commands.DelHomeCommand;
import ghostyhome.commands.HomeCommand;
import ghostyhome.commands.HomesCommand;
import ghostyhome.commands.ReloadCommand;
import ghostyhome.commands.SetHomeCommand;
import ghostyhome.listener.GUIListener;
import ghostyhome.manager.HomeManager;
import ghostyhome.manager.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GhostyHome extends JavaPlugin {

    private static GhostyHome instance;
    private HomeManager homeManager;
    private LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        langManager = new LangManager(this);
        homeManager = new HomeManager(this);

        HomeCommand homeCmd = new HomeCommand(this);
        getCommand("home").setExecutor(homeCmd);
        getCommand("home").setTabCompleter(homeCmd);

        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));

        DelHomeCommand delCmd = new DelHomeCommand(this);
        getCommand("delhome").setExecutor(delCmd);
        getCommand("delhome").setTabCompleter(delCmd);

        AdminHomeCommand adminCmd = new AdminHomeCommand(this);
        getCommand("adminhome").setExecutor(adminCmd);
        getCommand("adminhome").setTabCompleter(adminCmd);

        getCommand("ghreload").setExecutor(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("GhostyHome v" + getDescription().getVersion() + " gestartet!");
        getLogger().info("Sprache: " + getConfig().getString("language", "de_DE"));
    }

    @Override
    public void onDisable() {
        if (homeManager != null) homeManager.saveHomes();
        getLogger().info("GhostyHome wurde deaktiviert.");
    }

    public static GhostyHome getInstance() { return instance; }
    public HomeManager getHomeManager() { return homeManager; }
    public LangManager getLangManager() { return langManager; }
}
