package ghostyhome.gui;

import ghostyhome.GhostyHome;
import ghostyhome.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeGUI implements InventoryHolder {

    private final GhostyHome plugin;
    private final Player viewer;
    private final UUID targetUUID;
    private final String targetName;
    private Inventory inventory;
    private int page;
    private final List<String> homeNames;

    private static final int[] HOME_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int HOMES_PER_PAGE = HOME_SLOTS.length;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_NEXT = 53;

    public HomeGUI(GhostyHome plugin, Player viewer, UUID targetUUID, String targetName) {
        this.plugin     = plugin;
        this.viewer     = viewer;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.page       = 0;
        this.homeNames  = new ArrayList<>(plugin.getHomeManager().getHomes(targetUUID).keySet());
        Collections.sort(homeNames);
    }

    public void open() {
        buildInventory();
        viewer.openInventory(inventory);
    }

    private void buildInventory() {
        boolean isAdmin = !viewer.getUniqueId().equals(targetUUID);
        String title = isAdmin
                ? ColorUtil.colorize(plugin.getLangManager().get("gui.admin-title", targetName))
                : ColorUtil.colorize(plugin.getLangManager().get("gui.title"));

        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE,
                plugin.getLangManager().get("gui.filler-name"));
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        Map<String, Location> homes = plugin.getHomeManager().getHomes(targetUUID);
        int startIndex = page * HOMES_PER_PAGE;
        for (int i = 0; i < HOMES_PER_PAGE; i++) {
            int homeIndex = startIndex + i;
            if (homeIndex >= homeNames.size()) break;
            String name = homeNames.get(homeIndex);
            Location loc = homes.get(name);
            inventory.setItem(HOME_SLOTS[i], createHomeItem(name, loc, isAdmin));
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) homeNames.size() / HOMES_PER_PAGE));
        inventory.setItem(SLOT_PREV, page > 0
                ? createItem(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.prev-page")))
                : filler);
        inventory.setItem(SLOT_INFO, createItem(Material.BOOK,
                ColorUtil.colorize(plugin.getLangManager().get("gui.page-info", page + 1, totalPages))));
        inventory.setItem(SLOT_NEXT, page < totalPages - 1
                ? createItem(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.next-page")))
                : filler);
    }

    private ItemStack createHomeItem(String name, Location loc, boolean isAdmin) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ColorUtil.colorize(
                plugin.getLangManager().get("gui.home-item-name", capitalize(name))));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize(plugin.getLangManager().get(
                "gui.home-item-world", loc.getWorld() != null ? loc.getWorld().getName() : "?")));
        lore.add(ColorUtil.colorize(plugin.getLangManager().get(
                "gui.home-item-coords", round(loc.getX()), round(loc.getY()), round(loc.getZ()))));
        lore.add("");
        if (isAdmin) {
            lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-click-to-tp")));
        } else {
            lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.click-to-tp")));
            lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.shift-click-to-delete")));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Collections.emptyList());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(int slot, boolean isShift) {
        if (slot == SLOT_PREV && page > 0) {
            page--;
            buildInventory();
            viewer.openInventory(inventory);
            return;
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) homeNames.size() / HOMES_PER_PAGE));
        if (slot == SLOT_NEXT && page < totalPages - 1) {
            page++;
            buildInventory();
            viewer.openInventory(inventory);
            return;
        }
        for (int i = 0; i < HOME_SLOTS.length; i++) {
            if (HOME_SLOTS[i] != slot) continue;
            int homeIndex = page * HOMES_PER_PAGE + i;
            if (homeIndex >= homeNames.size()) return;

            String homeName = homeNames.get(homeIndex);
            boolean isAdmin = !viewer.getUniqueId().equals(targetUUID);

            if (isAdmin) {
                Location loc = plugin.getHomeManager().getHome(targetUUID, homeName);
                if (loc != null) {
                    viewer.closeInventory();
                    viewer.teleport(loc);
                    viewer.sendMessage(plugin.getLangManager().getPrefixed(
                            "cmd.home.teleported", capitalize(homeName)));
                }
            } else if (isShift) {
                viewer.closeInventory();
                plugin.getHomeManager().deleteHome(viewer, homeName);
                viewer.sendMessage(plugin.getLangManager().getPrefixed(
                        "cmd.delhome.success", capitalize(homeName)));
                homeNames.remove(homeName);
            } else {
                Location loc = plugin.getHomeManager().getHome(viewer, homeName);
                if (loc != null) {
                    viewer.closeInventory();
                    viewer.teleport(loc);
                    viewer.sendMessage(plugin.getLangManager().getPrefixed(
                            "cmd.home.teleported", capitalize(homeName)));
                }
            }
            return;
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }
    public Player getViewer() { return viewer; }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
