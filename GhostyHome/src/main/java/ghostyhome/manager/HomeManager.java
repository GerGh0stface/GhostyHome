package ghostyhome.manager;

import ghostyhome.GhostyHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class HomeManager {

    private final GhostyHome plugin;
    private File homesFile;
    private FileConfiguration homesConfig;
    private final Map<UUID, Map<String, Location>> homesCache = new HashMap<>();

    public HomeManager(GhostyHome plugin) {
        this.plugin = plugin;
        loadHomes();
    }

    public void loadHomes() {
        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Konnte homes.yml nicht erstellen!", e);
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        homesCache.clear();

        if (!homesConfig.contains("players")) return;

        for (String uuidStr : homesConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid;
            try { uuid = UUID.fromString(uuidStr); }
            catch (IllegalArgumentException e) { continue; }

            String basePath = "players." + uuidStr;
            if (homesConfig.getConfigurationSection(basePath) == null) continue;

            Map<String, Location> playerHomes = new HashMap<>();
            for (String homeName : homesConfig.getConfigurationSection(basePath).getKeys(false)) {
                String path = basePath + "." + homeName;
                String worldName = homesConfig.getString(path + ".world");
                if (worldName == null) continue;
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                double x     = homesConfig.getDouble(path + ".x");
                double y     = homesConfig.getDouble(path + ".y");
                double z     = homesConfig.getDouble(path + ".z");
                float  yaw   = (float) homesConfig.getDouble(path + ".yaw");
                float  pitch = (float) homesConfig.getDouble(path + ".pitch");
                playerHomes.put(homeName, new Location(world, x, y, z, yaw, pitch));
            }
            if (!playerHomes.isEmpty()) homesCache.put(uuid, playerHomes);
        }
    }

    public void saveHomes() {
        homesConfig.set("players", null);
        for (Map.Entry<UUID, Map<String, Location>> entry : homesCache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                String path = "players." + uuidStr + "." + homeEntry.getKey();
                Location loc = homeEntry.getValue();
                homesConfig.set(path + ".world", loc.getWorld().getName());
                homesConfig.set(path + ".x",     loc.getX());
                homesConfig.set(path + ".y",     loc.getY());
                homesConfig.set(path + ".z",     loc.getZ());
                homesConfig.set(path + ".yaw",   loc.getYaw());
                homesConfig.set(path + ".pitch", loc.getPitch());
            }
        }
        try { homesConfig.save(homesFile); }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Konnte homes.yml nicht speichern!", e);
        }
    }

    public Map<String, Location> getHomes(UUID uuid) {
        return homesCache.getOrDefault(uuid, new HashMap<>());
    }
    public Map<String, Location> getHomes(Player player) { return getHomes(player.getUniqueId()); }
    public Location getHome(UUID uuid, String name) { return getHomes(uuid).get(name.toLowerCase()); }
    public Location getHome(Player player, String name) { return getHome(player.getUniqueId(), name); }
    public boolean hasHome(Player player, String name) {
        return getHomes(player).containsKey(name.toLowerCase());
    }

    public boolean setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        int maxHomes = getMaxHomes(player);
        Map<String, Location> playerHomes = homesCache.computeIfAbsent(uuid, k -> new HashMap<>());
        if (!playerHomes.containsKey(name.toLowerCase()) && playerHomes.size() >= maxHomes) return false;
        playerHomes.put(name.toLowerCase(), player.getLocation().clone());
        saveHomes();
        return true;
    }

    public boolean deleteHome(Player player, String name) {
        Map<String, Location> playerHomes = getHomes(player);
        if (playerHomes.remove(name.toLowerCase()) != null) { saveHomes(); return true; }
        return false;
    }

    public int getMaxHomes(Player player) {
        for (int i = 100; i >= 1; i--)
            if (player.hasPermission("ghostyhome.homes." + i)) return i;
        return plugin.getConfig().getInt("default-max-homes", 5);
    }

    public int getHomeCount(Player player) { return getHomes(player).size(); }
}
