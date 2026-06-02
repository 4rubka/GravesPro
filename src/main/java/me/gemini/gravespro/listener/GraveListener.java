package me.gemini.gravespro.listener;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.manager.GraveViewGUI;
import me.gemini.gravespro.model.Grave;
import me.gemini.gravespro.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GraveListener implements Listener {

    private final GravesPro plugin;

    public GraveListener(GravesPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGraveInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Material graveMaterial = Material.valueOf(plugin.getConfigurationManager().getConfig("settings.yml").getString("grave.block", "PLAYER_HEAD"));
        if (block.getType() != graveMaterial) return;

        for (Grave grave : plugin.getGraveManager().getActiveGraves().values()) {
            if (grave.getLocation().getBlock().equals(block)) {
                event.setCancelled(true);
                openGrave(event.getPlayer(), grave);
                return;
            }
        }
    }

    private void openGrave(Player player, Grave grave) {
        // Protection check
        if (plugin.getConfigurationManager().getConfig("settings.yml").getBoolean("protection.owner-only") &&
            !grave.getOwnerId().equals(player.getUniqueId()) &&
            !player.hasPermission("gravespro.admin")) {
            player.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("no-permission")));
            return;
        }

        player.openInventory(new GraveViewGUI(plugin, grave).getInventory());
    }

    @EventHandler
    public void onGraveBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material graveMaterial = Material.valueOf(plugin.getConfigurationManager().getConfig("settings.yml").getString("grave.block", "PLAYER_HEAD"));
        if (block.getType() != graveMaterial) return;

        for (Grave grave : plugin.getGraveManager().getActiveGraves().values()) {
            if (grave.getLocation().getBlock().equals(block)) {
                if (plugin.getConfigurationManager().getConfig("settings.yml").getBoolean("protection.block-break-protection") &&
                    !grave.getOwnerId().equals(event.getPlayer().getUniqueId()) &&
                    !event.getPlayer().hasPermission("gravespro.admin")) {
                    event.setCancelled(true);
                    return;
                }
                // If it's the owner or admin breaking it, maybe we should drop the items?
                // For now, let's just cancel it and force them to use the GUI.
                event.setCancelled(true);
                openGrave(event.getPlayer(), grave);
                return;
            }
        }
    }
}
