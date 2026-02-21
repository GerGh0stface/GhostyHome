package de.ghosty.ghostyhome.gui;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUI that lists online players so an admin can select one and view their homes.
 */
public class AdminHomeGUI implements InventoryHolder {

    private final GhostyHome plugin;
    private final Player admin;
    private Inventory inventory;
    private int page;
    private List<Player> onlinePlayers;

    private static final int[] PLAYER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int PLAYERS_PER_PAGE = PLAYER_SLOTS.length;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_NEXT = 53;

    public AdminHomeGUI(GhostyHome plugin, Player admin) {
        this.plugin  = plugin;
        this.admin   = admin;
        this.page    = 0;
        this.onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public void open() {
        buildInventory();
        admin.openInventory(inventory);
    }

    private void buildInventory() {
        String title = ColorUtil.colorize(plugin.getLangManager().get("gui.admin-player-select-title"));
        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        int startIndex = page * PLAYERS_PER_PAGE;

        for (int i = 0; i < PLAYERS_PER_PAGE; i++) {
            int playerIndex = startIndex + i;
            if (playerIndex >= onlinePlayers.size()) break;

            Player target = onlinePlayers.get(playerIndex);
            ItemStack head = createPlayerHead(target);
            inventory.setItem(PLAYER_SLOTS[i], head);
        }

        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / PLAYERS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        inventory.setItem(SLOT_PREV, page > 0
                ? createItem(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.prev-page")))
                : filler);

        inventory.setItem(SLOT_INFO, createItem(Material.BOOK,
                ColorUtil.colorize(plugin.getLangManager().get("gui.page-info", page + 1, totalPages))));

        inventory.setItem(SLOT_NEXT, page < totalPages - 1
                ? createItem(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.next-page")))
                : filler);
    }

    private ItemStack createPlayerHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        meta.setOwningPlayer(target);
        meta.setDisplayName(ColorUtil.colorize(
                plugin.getLangManager().get("gui.admin-player-name", target.getName())));

        int homeCount = plugin.getHomeManager().getHomeCount(target);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-player-homes", homeCount)));
        lore.add("");
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-click-to-view")));
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
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

    public void handleClick(int slot) {
        if (slot == SLOT_PREV && page > 0) {
            page--;
            buildInventory();
            admin.openInventory(inventory);
            return;
        }

        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / PLAYERS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (slot == SLOT_NEXT && page < totalPages - 1) {
            page++;
            buildInventory();
            admin.openInventory(inventory);
            return;
        }

        for (int i = 0; i < PLAYER_SLOTS.length; i++) {
            if (PLAYER_SLOTS[i] == slot) {
                int playerIndex = page * PLAYERS_PER_PAGE + i;
                if (playerIndex >= onlinePlayers.size()) return;

                Player target = onlinePlayers.get(playerIndex);
                // Open HomeGUI for this player
                HomeGUI homeGUI = new HomeGUI(plugin, admin, target.getUniqueId(), target.getName());
                homeGUI.open();
                return;
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
