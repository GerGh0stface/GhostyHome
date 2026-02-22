package ghostyhome.commands;

import ghostyhome.GhostyHome;
import ghostyhome.gui.HomeGUI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final GhostyHome plugin;

    public HomeCommand(GhostyHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!player.hasPermission("ghostyhome.home")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        Map<String, Location> homes = plugin.getHomeManager().getHomes(player);
        if (args.length == 0) {
            if (homes.isEmpty()) {
                player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.no-homes"));
                return true;
            }
            if (homes.size() == 1) {
                teleport(player, homes.keySet().iterator().next());
                return true;
            }
            new HomeGUI(plugin, player, player.getUniqueId(), player.getName()).open();
            return true;
        }
        String homeName = args[0].toLowerCase();
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.not-found", args[0]));
            return true;
        }
        teleport(player, homeName);
        return true;
    }

    private void teleport(Player player, String homeName) {
        Location loc = plugin.getHomeManager().getHome(player, homeName);
        if (loc == null) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.not-found", homeName));
            return;
        }
        player.teleport(loc);
        player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.teleported", capitalize(homeName)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player player)) return completions;
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String name : plugin.getHomeManager().getHomes(player).keySet())
                if (name.startsWith(input)) completions.add(name);
        }
        return completions;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
