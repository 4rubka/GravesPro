package me.gemini.gravespro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grave {
    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private Location location;
    private long creationTime;
    private long expirationTime;
    private GraveState state;
    private Map<Integer, ItemStack> items; // Slot -> Item
    private int experience;
    private String deathCause;

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
}
