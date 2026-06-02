package me.gemini.gravespro.listener;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.DeathSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class DeathListener implements Listener {

    private final GravesPro plugin;

    public DeathListener(GravesPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (event.getKeepInventory()) return;

        // Capture all drops and their original slots if possible
        // Note: event.getDrops() is just a list of items that will drop.
        // To preserve slots, we capture the inventory directly.
        Map<Integer, ItemStack> itemsWithSlots = new HashMap<>();
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && !contents[i].getType().isAir()) {
                itemsWithSlots.put(i, contents[i].clone());
            }
        }

        int droppedExp = event.getDroppedExp();
        
        plugin.getLogger().info("DeathListener: Capturing " + itemsWithSlots.size() + " items for " + player.getName());

        if (itemsWithSlots.isEmpty() && droppedExp == 0) {
            plugin.getLogger().info("DeathListener: No items or XP to save, skipping grave creation.");
            return;
        }

        // Clear drops and XP from the event
        event.getDrops().clear();
        event.setDroppedExp(0);

        String deathCause = player.getLastDamageCause() != null ? 
                player.getLastDamageCause().getCause().name() : "UNKNOWN";

        // Create Grave with slot mapping
        plugin.getGraveManager().createGrave(player, player.getLocation(), itemsWithSlots, droppedExp, deathCause);

        // Create Snapshot
        DeathSnapshot snapshot = DeathSnapshot.builder()
                .id(UUID.randomUUID())
                .playerId(player.getUniqueId())
                .playerName(player.getName())
                .location(player.getLocation())
                .timestamp(System.currentTimeMillis())
                .inventory(Arrays.stream(player.getInventory().getStorageContents())
                        .filter(i -> i != null && !i.getType().isAir())
                        .collect(Collectors.toList()))
                .armor(Arrays.stream(player.getInventory().getArmorContents())
                        .filter(i -> i != null && !i.getType().isAir())
                        .collect(Collectors.toList()))
                .offHand(player.getInventory().getItemInOffHand())
                .experience(player.getTotalExperience())
                .deathCause(deathCause)
                .build();

        plugin.getStorageManager().getProvider().saveSnapshot(snapshot);
        plugin.getStatsManager().incrementDeath(player.getUniqueId());
    }
}
