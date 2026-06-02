package me.gemini.gravespro.manager;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.Grave;
import me.gemini.gravespro.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraveViewGUI extends GUIManager.GraveGUI {

    private final GravesPro plugin;
    private final Grave grave;

    public GraveViewGUI(GravesPro plugin, Grave grave) {
        super(plugin.getConfigurationManager().getConfig("gui.yml").getInt("grave-view.size", 54),
                plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.title", "Grave View")
                        .replace("<player_name>", grave.getOwnerName()));
        this.plugin = plugin;
        this.grave = grave;
        setup();
    }

    private void setup() {
        // Fill background
        ItemStack bg = new ItemStack(Material.valueOf(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.background.material", "BLACK_STAINED_GLASS_PANE")));
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.displayName(TextUtil.parse(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.background.name", " ")));
        bg.setItemMeta(bgMeta);
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // Put items from grave into the GUI
        // We show items in order, but we'll restore them to original slots
        int i = 0;
        for (ItemStack item : grave.getItems().values()) {
            if (i >= 45) break; // Limit to 45 items in view
            inventory.setItem(i++, item);
        }

        // Info item
        ItemStack info = new ItemStack(Material.valueOf(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.info.material", "PLAYER_HEAD")));
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(grave.getOwnerId()));
        }
        infoMeta.displayName(TextUtil.parse(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.info.name", "<gold>Grave Information")));

        List<String> lore = plugin.getConfigurationManager().getConfig("gui.yml").getStringList("grave-view.items.info.lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();
        for (String line : lore) {
            formattedLore.add(TextUtil.parse(line
                    .replace("<player_name>", grave.getOwnerName())
                    .replace("<death_cause>", grave.getDeathCause())
                    .replace("<x>", String.valueOf(grave.getLocation().getBlockX()))
                    .replace("<y>", String.valueOf(grave.getLocation().getBlockY()))
                    .replace("<z>", String.valueOf(grave.getLocation().getBlockZ()))
                    .replace("<date>", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(grave.getCreationTime())))
                    .replace("<remaining_time>", formatTime(grave.getRemainingTime()))));
        }
        infoMeta.lore(formattedLore);
        info.setItemMeta(infoMeta);
        inventory.setItem(plugin.getConfigurationManager().getConfig("gui.yml").getInt("grave-view.items.info.slot", 4), info);

        // Recover button
        ItemStack recover = new ItemStack(Material.valueOf(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.recover-all.material", "CHEST_MINECART")));
        ItemMeta recoverMeta = recover.getItemMeta();
        recoverMeta.displayName(TextUtil.parse(plugin.getConfigurationManager().getConfig("gui.yml").getString("grave-view.items.recover-all.name", "<green>Recover All")));
        recover.setItemMeta(recoverMeta);
        inventory.setItem(plugin.getConfigurationManager().getConfig("gui.yml").getInt("grave-view.items.recover-all.slot", 49), recover);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    @Override
    public void handle(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == plugin.getConfigurationManager().getConfig("gui.yml").getInt("grave-view.items.recover-all.slot", 49)) {
            recoverGrave(player);
        }
    }

    private void recoverGrave(Player player) {
        if (!grave.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("gravespro.admin")) {
            player.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("no-permission")));
            return;
        }

        // Restore items to original slots
        for (Map.Entry<Integer, ItemStack> entry : grave.getItems().entrySet()) {
            int originalSlot = entry.getKey();
            ItemStack item = entry.getValue();
            if (item == null) continue;

            // Try to put back in original slot
            ItemStack currentInSlot = player.getInventory().getItem(originalSlot);
            if (currentInSlot == null || currentInSlot.getType().isAir()) {
                player.getInventory().setItem(originalSlot, item);
            } else {
                // If original slot is occupied, add normally
                player.getInventory().addItem(item).values().forEach(remaining -> 
                    player.getWorld().dropItemNaturally(player.getLocation(), remaining));
            }
        }
        player.giveExp(grave.getExperience());
        
        plugin.getGraveManager().removeGrave(grave);
        player.closeInventory();
        player.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("grave-recovered")));
    }
}
