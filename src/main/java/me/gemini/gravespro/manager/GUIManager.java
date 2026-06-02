package me.gemini.gravespro.manager;

import me.gemini.gravespro.GravesPro;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Consumer;

public class GUIManager implements Listener {

    private final GravesPro plugin;

    public GUIManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof GraveGUI) {
            event.setCancelled(true);
            ((GraveGUI) holder).handle(event);
        }
    }

    public abstract static class GraveGUI implements InventoryHolder {
        protected final Inventory inventory;

        public GraveGUI(int size, String title) {
            this.inventory = Bukkit.createInventory(this, size, me.gemini.gravespro.util.TextUtil.format(title));
        }

        public abstract void handle(InventoryClickEvent event);

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
