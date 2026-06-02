package me.gemini.gravespro.hook;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.api.HologramProvider;
import me.gemini.gravespro.model.Grave;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InternalHologramProvider implements HologramProvider {

    private final GravesPro plugin;
    private final Map<UUID, TextDisplay> holograms = new HashMap<>();

    public InternalHologramProvider(GravesPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createHologram(Grave grave) {
        Location loc = grave.getLocation().clone().add(0.5, 0.6, 0.5);
        if (!loc.isChunkLoaded()) return;

        TextDisplay display = (TextDisplay) loc.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY);
        display.setBillboard(Display.Billboard.CENTER);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        
        updateText(display, grave);
        holograms.put(grave.getId(), display);
    }

    @Override
    public void updateHologram(Grave grave) {
        TextDisplay display = holograms.get(grave.getId());
        if (display == null || !display.isValid()) {
            createHologram(grave);
            return;
        }
        updateText(display, grave);
    }

    private void updateText(TextDisplay display, Grave grave) {
        List<String> lines = plugin.getConfigurationManager().getConfig("settings.yml").getStringList("hologram.lines");
        Component component = Component.empty();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i)
                    .replace("<player_name>", grave.getOwnerName())
                    .replace("<remaining_time>", formatTime(grave.getRemainingTime()));
            
            component = component.append(MiniMessage.miniMessage().deserialize(line));
            if (i < lines.size() - 1) {
                component = component.append(Component.newline());
            }
        }
        display.text(component);
    }

    @Override
    public void deleteHologram(Grave grave) {
        TextDisplay display = holograms.remove(grave.getId());
        if (display != null) {
            display.remove();
        }
    }

    @Override
    public void deleteAll() {
        holograms.values().forEach(TextDisplay::remove);
        holograms.clear();
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}
